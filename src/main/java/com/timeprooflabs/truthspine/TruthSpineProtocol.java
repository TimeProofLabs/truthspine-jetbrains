package com.timeprooflabs.truthspine;

import com.intellij.ide.BrowserUtil;
import com.intellij.notification.NotificationGroupManager;
import com.intellij.notification.NotificationType;
import com.intellij.openapi.project.Project;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

final class TruthSpineProtocol {
    private TruthSpineProtocol() {}

    static void connectProject(Project project) {
        String projectRoot = project.getBasePath();
        if (projectRoot == null || projectRoot.isBlank()) {
            notify(project, "Open a saved project before connecting TruthSpine.", NotificationType.WARNING);
            return;
        }
        String encodedRoot = URLEncoder.encode(projectRoot, StandardCharsets.UTF_8);
        BrowserUtil.browse("truthspine://connector/setup?host=jetbrains&project=" + encodedRoot);
        notify(project, "TruthSpine is connecting this project.", NotificationType.INFORMATION);
    }

    static void openApp(Project project) {
        BrowserUtil.browse("truthspine://open");
        notify(project, "Opening TruthSpine.", NotificationType.INFORMATION);
    }

    static void notify(Project project, String message, NotificationType type) {
        NotificationGroupManager.getInstance()
            .getNotificationGroup("TruthSpine")
            .createNotification(message, type)
            .notify(project);
    }
}
