package com.timeprooflabs.truthspine;

import com.intellij.ide.BrowserUtil;
import com.intellij.notification.NotificationGroupManager;
import com.intellij.notification.NotificationType;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.SystemInfo;
import java.io.IOException;
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
        String url = "truthspine://connector/setup?host=jetbrains&project=" + encodedRoot;
        if (openRegisteredApp(url)) {
            notify(project, "TruthSpine is connecting this project.", NotificationType.INFORMATION);
        } else {
            notify(project, "TruthSpine could not open. Start the TruthSpine app and try again.", NotificationType.WARNING);
        }
    }

    static void openApp(Project project) {
        if (openRegisteredApp("truthspine://open")) {
            notify(project, "Opening TruthSpine.", NotificationType.INFORMATION);
        } else {
            notify(project, "TruthSpine could not open. Start the TruthSpine app and try again.", NotificationType.WARNING);
        }
    }

    private static boolean openRegisteredApp(String url) {
        try {
            ProcessBuilder launcher;
            if (SystemInfo.isWindows) {
                launcher = new ProcessBuilder("cmd.exe", "/d", "/c", "start", "", url);
            } else if (SystemInfo.isMac) {
                launcher = new ProcessBuilder("open", url);
            } else {
                launcher = new ProcessBuilder("xdg-open", url);
            }
            launcher.start();
            return true;
        } catch (IOException ignored) {
            BrowserUtil.browse(url);
            return false;
        }
    }

    static void notify(Project project, String message, NotificationType type) {
        NotificationGroupManager.getInstance()
            .getNotificationGroup("TruthSpine")
            .createNotification(message, type)
            .notify(project);
    }
}
