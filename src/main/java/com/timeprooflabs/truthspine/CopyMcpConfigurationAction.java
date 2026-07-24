package com.timeprooflabs.truthspine;

import com.intellij.notification.NotificationType;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.ide.CopyPasteManager;
import com.intellij.openapi.project.Project;
import java.awt.datatransfer.StringSelection;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import org.jetbrains.annotations.NotNull;

public final class CopyMcpConfigurationAction extends AnAction {
    @Override
    public void actionPerformed(@NotNull AnActionEvent event) {
        Project project = event.getProject();
        String projectRoot = project == null ? null : project.getBasePath();
        if (projectRoot == null || projectRoot.isBlank()) {
            TruthSpineProtocol.notify(project, "Open a saved project before copying its MCP configuration.", NotificationType.WARNING);
            return;
        }

        Path configuration = Path.of(projectRoot, "runtime", "connector-packages", "jetbrains", "truthspine-mcp.json");
        if (!Files.isRegularFile(configuration)) {
            TruthSpineProtocol.notify(project, "Connect this project to TruthSpine first.", NotificationType.WARNING);
            return;
        }

        try {
            String json = Files.readString(configuration, StandardCharsets.UTF_8).trim();
            CopyPasteManager.getInstance().setContents(new StringSelection(json));
            TruthSpineProtocol.notify(
                project,
                "MCP configuration copied. Add it in Settings > Tools > AI Assistant > Model Context Protocol.",
                NotificationType.INFORMATION
            );
        } catch (IOException error) {
            TruthSpineProtocol.notify(project, "TruthSpine could not read this project's MCP configuration.", NotificationType.ERROR);
        }
    }

    @Override
    public void update(@NotNull AnActionEvent event) {
        event.getPresentation().setEnabled(event.getProject() != null);
    }
}
