package com.timeprooflabs.truthspine;

import com.intellij.ide.BrowserUtil;
import com.intellij.notification.NotificationType;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.ide.util.PropertiesComponent;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.SystemInfo;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.jetbrains.annotations.Nullable;

final class AcquisitionJourney {
    private static final String AUTHORITY_URL = "https://license.truthspine.app";
    private static final String KEY_PREFIX = "truthspine.acquisition.";
    private static final Pattern JSON_STRING = Pattern.compile("\\\"([^\\\"]+)\\\"\\s*:\\s*\\\"([^\\\"]*)\\\"");
    private static final HttpClient HTTP = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(10)).build();

    private AcquisitionJourney() {}

    static void beginStoreHandoff(@Nullable Project project, String content) {
        String projectRoot = project == null || project.getBasePath() == null ? "" : project.getBasePath();
        CompletableFuture.runAsync(() -> {
            try {
                String platform = SystemInfo.isWindows ? "win32" : SystemInfo.isMac ? "darwin" : "linux";
                String requestBody = "{\"source\":\"jetbrains_marketplace\",\"medium\":\"plugin\","
                    + "\"connectorId\":\"jetbrains\",\"platform\":\"" + platform + "\","
                    + "\"campaign\":\"connector-install\",\"content\":\"" + json(content) + "\"}";
                HttpResponse<String> response = post("/v1/acquisition/journeys", requestBody);
                if (response.statusCode() != 201) throw new IllegalStateException("acquisition session was not created");
                String sessionId = field(response.body(), "sessionId");
                String journeyId = field(response.body(), "journeyId");
                String resumeToken = field(response.body(), "resumeToken");
                String expiresAt = field(response.body(), "expiresAt");
                if (!sessionId.matches("acqs_[a-f0-9]{32}") || !resumeToken.matches("[a-f0-9]{64}")) {
                    throw new IllegalStateException("acquisition session was invalid");
                }
                PropertiesComponent state = stateFor(project);
                state.setValue(KEY_PREFIX + "sessionId", sessionId);
                state.setValue(KEY_PREFIX + "journeyId", journeyId);
                state.setValue(KEY_PREFIX + "resumeToken", resumeToken);
                state.setValue(KEY_PREFIX + "expiresAt", expiresAt);
                state.setValue(KEY_PREFIX + "resumeRequestedAt", "");
                ApplicationManager.getApplication().invokeLater(() -> {
                    BrowserUtil.browse(storeUrl());
                    TruthSpineProtocol.notify(project, (SystemInfo.isLinux ? "Opening the TruthSpine download page for Linux." : "Opening TruthSpine in the correct app store.")
                        + " This project will resume after the app and trial are ready.", NotificationType.INFORMATION);
                });
                reportStage(sessionId, resumeToken, "store_handoff_opened");
            } catch (Exception error) {
                ApplicationManager.getApplication().invokeLater(() -> {
                    BrowserUtil.browse(storeUrl());
                    TruthSpineProtocol.notify(project, SystemInfo.isLinux ? "Opening the TruthSpine download page for Linux." : "Opening the official TruthSpine app-store download page.", NotificationType.INFORMATION);
                });
            }
        });
    }

    static void resumeIfReady(Project project) {
        if (project.isDisposed() || !TruthSpineSetup.projectApproved(project)) return;
        PropertiesComponent state = stateWithPending(project);
        String sessionId = state.getValue(KEY_PREFIX + "sessionId", "");
        String resumeToken = state.getValue(KEY_PREFIX + "resumeToken", "");
        String expiresAt = state.getValue(KEY_PREFIX + "expiresAt", "");
        if (sessionId.isBlank() || resumeToken.isBlank()) return;
        try {
            if (!Instant.parse(expiresAt).isAfter(Instant.now())) {
                return;
            }
        } catch (RuntimeException error) {
            // Refuse invalid saved state without silently erasing diagnostic history.
            return;
        }
        String priorRequest = state.getValue(KEY_PREFIX + "resumeRequestedAt", "");
        try {
            if (!priorRequest.isBlank()) {
                Instant requestedAt = Instant.parse(priorRequest);
                Instant now = Instant.now();
                if (!requestedAt.isAfter(now) && requestedAt.plusSeconds(300).isAfter(now)) return;
            }
        } catch (RuntimeException ignored) {}
        if (!TruthSpineProtocol.appInstalled()) return;
        String projectRoot = project.getBasePath();
        String url = "truthspine://acquisition/resume?session=" + encode(sessionId)
            + "&token=" + encode(resumeToken)
            + "&host=jetbrains"
            + (projectRoot == null || projectRoot.isBlank() ? "" : "&project=" + encode(projectRoot));
        ApplicationManager.getApplication().invokeLater(() -> {
            if (project.isDisposed() || !TruthSpineSetup.projectApproved(project)
                || !java.util.Objects.equals(projectRoot, project.getBasePath())
                || !sessionId.equals(state.getValue(KEY_PREFIX + "sessionId", ""))
                || !resumeToken.equals(state.getValue(KEY_PREFIX + "resumeToken", ""))
                || !expiresAt.equals(state.getValue(KEY_PREFIX + "expiresAt", ""))
                || !priorRequest.equals(state.getValue(KEY_PREFIX + "resumeRequestedAt", ""))) return;
            try {
                if (!Instant.parse(expiresAt).isAfter(Instant.now())) return;
            } catch (RuntimeException ignored) { return; }
            if (!TruthSpineProtocol.openRegisteredApp(url)) {
                TruthSpineProtocol.notify(project, "Could not request connector resume. No connection was confirmed; you can retry.", NotificationType.WARNING);
                return;
            }
            state.setValue(KEY_PREFIX + "resumeRequestedAt", Instant.now().toString());
            reportStage(sessionId, resumeToken, "connector_resume_requested");
            TruthSpineProtocol.notify(project, "Connector resume requested. Project connection and Store access have not yet been confirmed.", NotificationType.INFORMATION);
        });
    }

    private static void reportStage(String sessionId, String resumeToken, String action) {
        CompletableFuture.runAsync(() -> {
            try {
                post("/v1/acquisition/journeys/event", "{\"sessionId\":\"" + json(sessionId)
                    + "\",\"resumeToken\":\"" + json(resumeToken)
                    + "\",\"action\":\"" + json(action)
                    + "\",\"eventId\":\"acq_" + json(action) + "_" + UUID.randomUUID().toString().replace("-", "") + "\"}");
            } catch (Exception ignored) {}
        });
    }

    private static HttpResponse<String> post(String route, String body) throws Exception {
        HttpRequest request = HttpRequest.newBuilder(URI.create(AUTHORITY_URL + route))
            .timeout(Duration.ofSeconds(10))
            .header("content-type", "application/json")
            .POST(HttpRequest.BodyPublishers.ofString(body, StandardCharsets.UTF_8))
            .build();
        return HTTP.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
    }

    private static String field(String body, String name) {
        Matcher matcher = JSON_STRING.matcher(body);
        while (matcher.find()) if (matcher.group(1).equals(name)) return matcher.group(2);
        return "";
    }

    private static String encode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }

    private static String storeUrl() {
        if (SystemInfo.isWindows) return "https://apps.microsoft.com/detail/9pbn25k75vdj";
        if (SystemInfo.isMac) return "https://apps.apple.com/us/app/truthspine/id6794478470?mt=12";
        return TruthSpineProtocol.DOWNLOAD_URL;
    }

    private static String json(String value) {
        return String.valueOf(value).replace("\\", "\\\\").replace("\"", "\\\"");
    }

    private static PropertiesComponent stateFor(@Nullable Project project) {
        return project == null ? PropertiesComponent.getInstance() : PropertiesComponent.getInstance(project);
    }

    private static PropertiesComponent stateWithPending(Project project) {
        PropertiesComponent projectState = PropertiesComponent.getInstance(project);
        if (!projectState.getValue(KEY_PREFIX + "sessionId", "").isBlank()) return projectState;
        return PropertiesComponent.getInstance();
    }

}
