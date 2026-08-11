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
import org.jetbrains.annotations.NotNull;

public final class CopyMcpConfigurationAction extends AnAction {
    private static String jsonEscape(String value) {
        return value.replace("\\", "\\\\").replace("\"", "\\\"");
    }

    private static String macAppStoreConfiguration(String projectRoot) {
        String home = System.getProperty("user.home");
        Path systemApp = Path.of("/Applications/TruthSpine.app");
        Path userApp = Path.of(home, "Applications", "TruthSpine.app");
        Path app = Files.isExecutable(systemApp.resolve("Contents/MacOS/TruthSpine")) ? systemApp : userApp;
        Path executable = app.resolve("Contents/MacOS/TruthSpine");
        Path proxy = app.resolve("Contents/Resources/app.asar/src/local-app-mcp-proxy.js");
        if (!Files.isExecutable(executable)) return null;
        String command = jsonEscape(executable.toString());
        String proxyPath = jsonEscape(proxy.toString());
        String root = jsonEscape(projectRoot);
        return "{\n" +
            "  \"mcpServers\": {\n" +
            "    \"truthspine\": {\n" +
            "      \"type\": \"stdio\",\n" +
            "      \"command\": \"" + command + "\",\n" +
            "      \"args\": [\"" + proxyPath + "\", \"--host-id\", \"jetbrains\", \"--project-root\", \"" + root + "\"],\n" +
            "      \"env\": {\"ELECTRON_RUN_AS_NODE\": \"1\", \"TRUTHSPINE_PACKAGED_RUNTIME\": \"1\"}\n" +
            "    }\n" +
            "  }\n" +
            "}";
    }

    static boolean copyProjectConfiguration(Project project, boolean openSettings) {
        String projectRoot = project == null ? null : project.getBasePath();
        if (projectRoot == null || projectRoot.isBlank()) {
            TruthSpineProtocol.notify(project, "Open a saved project before copying its MCP configuration.", NotificationType.WARNING);
            return false;
        }

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
                : Files.readString(configuration, StandardCharsets.UTF_8).trim();
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
            TruthSpineProtocol.notify(project, "TruthSpine could not read this project's MCP configuration.", NotificationType.ERROR);
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
