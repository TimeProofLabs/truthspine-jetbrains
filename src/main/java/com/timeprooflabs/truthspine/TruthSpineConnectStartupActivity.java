package com.timeprooflabs.truthspine;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.startup.StartupActivity;
import com.intellij.util.Alarm;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.LinkOption;
import java.nio.file.StandardCopyOption;
import java.time.Instant;
import org.jetbrains.annotations.NotNull;

public final class TruthSpineConnectStartupActivity implements StartupActivity.DumbAware {

    @Override
    public void runActivity(@NotNull Project project) {
        TruthSpineSetup.showIfNeeded(project);
        Alarm monitor = new Alarm(Alarm.ThreadToUse.POOLED_THREAD, project);
        Runnable[] check = new Runnable[1];
        check[0] = () -> {
            AcquisitionJourney.resumeIfReady(project);
            TruthSpineSetup.completeIfInstalled(project);
            consumeRequest(project);
            if (!project.isDisposed()) monitor.addRequest(check[0], 1000);
        };
        monitor.addRequest(check[0], 250);
    }

    private static void consumeRequest(Project project) {
        if (!TruthSpineSetup.projectApproved(project)) return;
        String projectRoot = project.getBasePath();
        if (projectRoot == null || projectRoot.isBlank()) return;
        Path requestPath = Path.of(projectRoot, ".truthspine", "runtime", "jetbrains-connect-request.json");
        if (!Files.isRegularFile(requestPath, LinkOption.NOFOLLOW_LINKS)) return;

        try {
            Path physicalRoot = Path.of(projectRoot).toRealPath();
            if (!requestPath.getParent().toRealPath().equals(physicalRoot.resolve(".truthspine").resolve("runtime"))) return;
            byte[] bytes;
            try (var input = Files.newInputStream(requestPath)) { bytes = input.readNBytes(ConnectionRequestContract.MAX_BYTES + 1); }
            if (bytes.length > ConnectionRequestContract.MAX_BYTES) return;
            String request = StandardCharsets.UTF_8.newDecoder().decode(java.nio.ByteBuffer.wrap(bytes)).toString();
            if (!ConnectionRequestContract.valid(request, projectRoot, Instant.now())) return;
            String approvedScope = ProjectApprovalScope.identity(projectRoot);
            ApplicationManager.getApplication().invokeLater(() -> {
                if (project.isDisposed() || !TruthSpineSetup.projectApproved(project)
                    || !ProjectApprovalScope.matches(project.getBasePath(), approvedScope)
                    || !ConnectionRequestContract.valid(request, project.getBasePath(), Instant.now())) return;
                try {
                    if (!Files.isRegularFile(requestPath, LinkOption.NOFOLLOW_LINKS)
                        || !requestPath.getParent().toRealPath().equals(physicalRoot.resolve(".truthspine").resolve("runtime"))) return;
                    byte[] current;
                    try (var input = Files.newInputStream(requestPath)) { current = input.readNBytes(ConnectionRequestContract.MAX_BYTES + 1); }
                    if (!java.util.Arrays.equals(bytes, current)) return;
                    // Retain a claimed request, not a receipt of completed connection.
                    Files.move(requestPath, requestPath.resolveSibling("jetbrains-connect-request-" + java.util.UUID.randomUUID() + ".claimed.json"), StandardCopyOption.ATOMIC_MOVE);
                } catch (IOException | RuntimeException error) { return; }
                if (!TruthSpineProtocol.connectProject(project)) return;
                CopyMcpConfigurationAction.copyProjectConfiguration(project, true);
            });
        } catch (IOException | RuntimeException ignored) {
            // Invalid/unreadable requests remain available for explicit recovery.
        }
    }
}
