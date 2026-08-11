package com.timeprooflabs.truthspine;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.startup.StartupActivity;
import com.intellij.util.Alarm;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.jetbrains.annotations.NotNull;

public final class TruthSpineConnectStartupActivity implements StartupActivity.DumbAware {
    private static final Pattern EXPIRES_AT = Pattern.compile("\\\"expiresAt\\\"\\s*:\\s*\\\"([^\\\"]+)\\\"");

    @Override
    public void runActivity(@NotNull Project project) {
        Alarm monitor = new Alarm(Alarm.ThreadToUse.POOLED_THREAD, project);
        Runnable[] check = new Runnable[1];
        check[0] = () -> {
            consumeRequest(project);
            if (!project.isDisposed()) monitor.addRequest(check[0], 1000);
        };
        monitor.addRequest(check[0], 250);
    }

    private static void consumeRequest(Project project) {
        String projectRoot = project.getBasePath();
        if (projectRoot == null || projectRoot.isBlank()) return;
        Path requestPath = Path.of(projectRoot, ".truthspine", "runtime", "jetbrains-connect-request.json");
        if (!Files.isRegularFile(requestPath)) return;

        try {
            String request = Files.readString(requestPath, StandardCharsets.UTF_8);
            Files.deleteIfExists(requestPath);
            if (!request.contains("truthspine.jetbrains-connect-request.v1") || !request.contains("\"hostId\": \"jetbrains\"")) {
                return;
            }
            Matcher expiresAt = EXPIRES_AT.matcher(request);
            if (!expiresAt.find() || Instant.parse(expiresAt.group(1)).isBefore(Instant.now())) return;
            ApplicationManager.getApplication().invokeLater(() -> {
                TruthSpineProtocol.connectProject(project);
                CopyMcpConfigurationAction.copyProjectConfiguration(project, true);
            });
        } catch (IOException | RuntimeException ignored) {
            try {
                Files.deleteIfExists(requestPath);
            } catch (IOException ignoredAgain) {
                // The next app attempt replaces an unreadable stale request.
            }
        }
    }
}
