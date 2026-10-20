package com.timeprooflabs.truthspine;

import com.intellij.notification.NotificationType;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.ide.CopyPasteManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.options.ShowSettingsUtil;
import java.awt.datatransfer.StringSelection;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.LinkOption;
import org.jetbrains.annotations.NotNull;

public final class CopyMcpConfigurationAction extends AnAction {
    private static final int MAX_CONFIGURATION_BYTES = 65536;

    private static String readConfiguration(Path configuration, String projectRoot) throws IOException {
        Path expectedParent = Path.of(projectRoot).toRealPath().resolve("runtime").resolve("connector-packages").resolve("jetbrains");
        if (!configuration.getParent().toRealPath().equals(expectedParent)
            || !Files.isRegularFile(configuration, LinkOption.NOFOLLOW_LINKS)) {
            throw new IOException("Configuration must be a local project file");
        }
        byte[] bytes;
        try (var input = Files.newInputStream(configuration, LinkOption.NOFOLLOW_LINKS)) {
            bytes = input.readNBytes(MAX_CONFIGURATION_BYTES + 1);
        }
        if (bytes.length > MAX_CONFIGURATION_BYTES) throw new IOException("Configuration exceeds size limit");
        return StandardCharsets.UTF_8.newDecoder().decode(java.nio.ByteBuffer.wrap(bytes)).toString().trim();
    }

    private static String macAppStoreConfiguration(String projectRoot) {
        String home = System.getProperty("user.home");
        Path systemApp = Path.of("/Applications/TruthSpine.app");
        Path userApp = Path.of(home, "Applications", "TruthSpine.app");
        Path app = Files.isExecutable(systemApp.resolve("Contents/MacOS/TruthSpine")) ? systemApp : userApp;
        Path executable = app.resolve("Contents/MacOS/TruthSpine");
        Path proxy = app.resolve("Contents/Resources/app.asar/src/local-app-mcp-proxy.js");
        if (!Files.isExecutable(executable)) return null;
        return new com.google.gson.Gson().toJson(java.util.Map.of("mcpServers", java.util.Map.of("truthspine", java.util.Map.of(
            "type", "stdio", "command", executable.toString(),
            // The IDE starts the server in the project folder; the entry names no project (owner's rule 2026-10-09).
            "args", java.util.List.of(proxy.toString(), "--host-id", "jetbrains", "--project-from-cwd"),
            "env", java.util.Map.of("ELECTRON_RUN_AS_NODE", "1", "TRUTHSPINE_PACKAGED_RUNTIME", "1",
                "TRUTHSPINE_HOST_ID", "jetbrains", "TRUTHSPINE_PROJECT_FROM_CWD", "1")
        ))));
    }

    static boolean copyProjectConfiguration(Project project, boolean openSettings) {
        if (project == null || project.isDisposed()) return false;
        if (!TruthSpineSetup.projectApproved(project)) {
            TruthSpineProtocol.notify(project, "Use Connect Current Project and approve this folder before copying its MCP configuration.", NotificationType.WARNING);
            return false;
        }
        String projectRoot = project == null ? null : project.getBasePath();
        if (projectRoot == null || projectRoot.isBlank()) {
            TruthSpineProtocol.notify(project, "Open a saved project before copying its MCP configuration.", NotificationType.WARNING);
            return false;
        }
        String approvedScope = ProjectApprovalScope.identity(projectRoot);
        if (approvedScope == null) return false;

        Path configuration = Path.of(projectRoot, "runtime", "connector-packages", "jetbrains", "truthspine-mcp.json");
        String generatedMacConfiguration = System.getProperty("os.name", "").toLowerCase().contains("mac")
            ? macAppStoreConfiguration(projectRoot)
            : null;
        if (!Files.isRegularFile(configuration) && generatedMacConfiguration == null) {
            TruthSpineProtocol.notify(project, "Connect this project to TruthSpine first.", NotificationType.WARNING);
            return false;
        }

        try {
            String json = generatedMacConfiguration != null
                ? generatedMacConfiguration
                : readConfiguration(configuration, projectRoot);
            if (!McpConfigurationContract.valid(json, projectRoot)) {
                TruthSpineProtocol.notify(project, "This MCP configuration is invalid or does not match this project. Reconnect this project to regenerate it.", NotificationType.WARNING);
                return false;
            }
            if (project.isDisposed() || !TruthSpineSetup.projectApproved(project)
                || !ProjectApprovalScope.matches(project.getBasePath(), approvedScope)) return false;
            CopyPasteManager.getInstance().setContents(new StringSelection(json));
            TruthSpineProtocol.notify(
                project,
                openSettings
                    ? "Project connection copied. Choose Add, select As JSON, paste it, set Level to Project, then choose Apply."
                    : "MCP configuration copied. Add it in Settings > Tools > AI Assistant > Model Context Protocol.",
                NotificationType.INFORMATION
            );
            if (openSettings) {
                ShowSettingsUtil.getInstance().showSettingsDialog(project, "Model Context Protocol (MCP)");
            }
            return true;
        } catch (IOException error) {
            TruthSpineProtocol.notify(project, "TruthSpine could not safely read this project's MCP configuration. Reconnect this project to regenerate it.", NotificationType.ERROR);
            return false;
        }
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent event) {
        copyProjectConfiguration(event.getProject(), false);
    }

    @Override
    public void update(@NotNull AnActionEvent event) {
        event.getPresentation().setEnabled(event.getProject() != null);
    }
}
