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
    // Owner 2026-10-06: Linux is offered at release (the AppImage from truthspine.app) alongside Windows and macOS.
    static final String UNSUPPORTED_PLATFORM_MESSAGE = "TruthSpine runs on Windows, macOS and Linux. This computer's system is not one of them.";
    static final String DOWNLOAD_URL = "https://truthspine.app/download"
        + "?utm_source=jetbrains_marketplace"
        + "&utm_medium=plugin"
        + "&utm_campaign=connector-install"
        + "&utm_content=download-app";

    private TruthSpineProtocol() {}

    // True means an OS handoff was requested, never a completed connection.
    static boolean connectProject(Project project) {
        if (project == null || project.isDisposed()) return false;
        if (!connectionSupported()) {
            notify(project, UNSUPPORTED_PLATFORM_MESSAGE, NotificationType.WARNING);
            return false;
        }
        String projectRoot = project.getBasePath();
        if (projectRoot == null || projectRoot.isBlank()) {
            notify(project, "Open a saved project before connecting TruthSpine.", NotificationType.WARNING);
            return false;
        }
        if (!appInstalled()) {
            AcquisitionJourney.beginStoreHandoff(project, "connect-project");
            return false;
        }
        String encodedRoot = URLEncoder.encode(projectRoot, StandardCharsets.UTF_8);
        String url = "truthspine://connector/setup?host=jetbrains&project=" + encodedRoot;
        if (openRegisteredApp(url)) {
            notify(project, "Requested this project's connection in TruthSpine. Connection is not yet verified.", NotificationType.INFORMATION);
            return true;
        } else {
            notify(project, "TruthSpine could not open. Start the TruthSpine app and try again.", NotificationType.WARNING);
            return false;
        }
    }

    static void openApp(Project project) {
        if (!connectionSupported()) {
            notify(project, UNSUPPORTED_PLATFORM_MESSAGE, NotificationType.WARNING);
            return;
        }
        if (!appInstalled()) {
            AcquisitionJourney.beginStoreHandoff(project, "open-app");
        } else if (openRegisteredApp("truthspine://open")) {
            notify(project, "Opening TruthSpine.", NotificationType.INFORMATION);
        } else {
            notify(project, "TruthSpine could not open. Start the TruthSpine app and try again.", NotificationType.WARNING);
        }
    }

    static void downloadApp(Project project) {
        if (!commercialDesktopSupported()) {
            notify(project, UNSUPPORTED_PLATFORM_MESSAGE, NotificationType.WARNING);
            return;
        }
        AcquisitionJourney.beginStoreHandoff(project, "download-app");
    }

    private static boolean commercialDesktopSupported() {
        return SystemInfo.isWindows || SystemInfo.isMac || SystemInfo.isLinux;
    }

    // Connecting needs the installed app; without it, the plugin offers getting it (on Linux, the AppImage download
    // page on truthspine.app, sold directly by TimeProofLabs).
    private static boolean connectionSupported() {
        return commercialDesktopSupported();
    }

    static boolean appInstalled() {
        if (SystemInfo.isMac) {
            String home = System.getProperty("user.home", "");
            return java.nio.file.Files.isExecutable(java.nio.file.Path.of("/Applications/TruthSpine.app/Contents/MacOS/TruthSpine"))
                || java.nio.file.Files.isExecutable(java.nio.file.Path.of(home, "Applications", "TruthSpine.app", "Contents", "MacOS", "TruthSpine"));
        }
        if (SystemInfo.isWindows) {
            // The app registers in ~\.truthspine\app (a Store app's own AppData folder is private to it); an earlier
            // app registered under %LOCALAPPDATA%\TruthSpine.
            String home = System.getProperty("user.home", "");
            String local = System.getenv("LOCALAPPDATA");
            return java.nio.file.Files.isRegularFile(java.nio.file.Path.of(home, ".truthspine", "app", "runtime.json"))
                || (local != null && java.nio.file.Files.isRegularFile(java.nio.file.Path.of(local, "TruthSpine", "runtime.json")));
        }
        if (SystemInfo.isLinux) {
            // The AppImage writes its runtime registration here each time it starts.
            String configured = System.getenv("XDG_CONFIG_HOME");
            java.nio.file.Path config = configured != null && !configured.isBlank()
                ? java.nio.file.Path.of(configured) : java.nio.file.Path.of(System.getProperty("user.home", ""), ".config");
            return java.nio.file.Files.isRegularFile(config.resolve("TruthSpine").resolve("runtime.json"));
        }
        return false;
    }

    static boolean openRegisteredApp(String url) {
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
