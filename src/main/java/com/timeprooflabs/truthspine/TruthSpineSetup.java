package com.timeprooflabs.truthspine;

import com.intellij.notification.Notification;
import com.intellij.notification.NotificationAction;
import com.intellij.notification.NotificationGroupManager;
import com.intellij.notification.NotificationType;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.ide.util.PropertiesComponent;
import com.intellij.openapi.project.Project;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.jetbrains.annotations.NotNull;

final class TruthSpineSetup {
    private static final String PROJECT_APPROVED_KEY = "truthspine.projectReadApproved.v1";
    private static final String PROJECT_APPROVED_SCOPE_KEY = "truthspine.projectReadApprovedScope.v2";
    private static final Map<Project, Notification> INSTALL_ACTIVE = new ConcurrentHashMap<>();
    private static final Map<Project, Notification> CONNECTION_ACTIVE = new ConcurrentHashMap<>();

    private TruthSpineSetup() {}

    static void showIfNeeded(Project project) {
        if (TruthSpineProtocol.appInstalled() || INSTALL_ACTIVE.containsKey(project)) {
            completeIfInstalled(project);
            return;
        }
        ApplicationManager.getApplication().invokeLater(() -> {
            if (project.isDisposed() || TruthSpineProtocol.appInstalled() || INSTALL_ACTIVE.containsKey(project)) return;
            Notification notification = NotificationGroupManager.getInstance()
                .getNotificationGroup("TruthSpine Setup")
                .createNotification(
                    "Finish TruthSpine Connector setup",
                    "This plugin is the connector. Install the protected desktop app to use TruthSpine, then this setup will resume automatically.",
                    NotificationType.INFORMATION
                );
            notification.setSuggestionType(true);
            notification.addAction(new NotificationAction("Install desktop app") {
                @Override
                public void actionPerformed(@NotNull AnActionEvent event, @NotNull Notification current) {
                    AcquisitionJourney.beginStoreHandoff(project, "first-run");
                }
            });
            INSTALL_ACTIVE.put(project, notification);
            notification.notify(project);
        });
    }

    static void completeIfInstalled(Project project) {
        if (!TruthSpineProtocol.appInstalled()) return;
        Notification notification = INSTALL_ACTIVE.remove(project);
        if (notification != null) {
            ApplicationManager.getApplication().invokeLater(notification::expire);
        }
        offerCurrentProject(project);
    }

    static boolean projectApproved(Project project) {
        if (project == null || project.isDisposed()) return false;
        PropertiesComponent state = PropertiesComponent.getInstance(project);
        return state.getBoolean(PROJECT_APPROVED_KEY, false)
            && ProjectApprovalScope.matches(project.getBasePath(), state.getValue(PROJECT_APPROVED_SCOPE_KEY, ""));
    }

    static void connectWithApproval(Project project) {
        if (!TruthSpineProtocol.appInstalled()) {
            AcquisitionJourney.beginStoreHandoff(project, "connect-project");
            return;
        }
        if (projectApproved(project)) {
            TruthSpineProtocol.connectProject(project);
            return;
        }
        // An explicit retry may replace an expired prompt. Background polling
        // does not repeatedly reopen a prompt the user dismissed.
        Notification prior = CONNECTION_ACTIVE.get(project);
        if (prior != null && prior.isExpired()) CONNECTION_ACTIVE.remove(project, prior);
        offerCurrentProject(project);
    }

    private static void offerCurrentProject(Project project) {
        if (project.isDisposed() || projectApproved(project) || CONNECTION_ACTIVE.containsKey(project)) return;
        String projectRoot = project.getBasePath();
        if (projectRoot == null || projectRoot.isBlank()) return;
        String offeredIdentity = ProjectApprovalScope.identity(projectRoot);
        if (offeredIdentity == null) return;
        ApplicationManager.getApplication().invokeLater(() -> {
            if (project.isDisposed() || projectApproved(project) || CONNECTION_ACTIVE.containsKey(project)) return;
            if (!ProjectApprovalScope.matches(project.getBasePath(), offeredIdentity)) return;
            String projectName = project.getName();
            Notification notification = NotificationGroupManager.getInstance()
                .getNotificationGroup("TruthSpine Setup")
                .createNotification(
                    "Connect this project to TruthSpine?",
                    "TruthSpine found the project already open in this IDE. Approve local reading and connection for "
                        + html(projectName) + ":<br><code>" + html(projectRoot) + "</code>",
                    NotificationType.INFORMATION
                );
            notification.setSuggestionType(true);
            notification.addAction(new NotificationAction("Connect " + projectName) {
                @Override
                public void actionPerformed(@NotNull AnActionEvent event, @NotNull Notification current) {
                    if (project.isDisposed() || !ProjectApprovalScope.matches(project.getBasePath(), offeredIdentity)) {
                        CONNECTION_ACTIVE.remove(project);
                        current.expire();
                        TruthSpineProtocol.notify(project, "The project changed since this approval was offered. Connect again to approve the current folder.", NotificationType.WARNING);
                        return;
                    }
                    PropertiesComponent.getInstance(project).setValue(PROJECT_APPROVED_SCOPE_KEY, offeredIdentity);
                    PropertiesComponent.getInstance(project).setValue(PROJECT_APPROVED_KEY, true);
                    CONNECTION_ACTIVE.remove(project);
                    current.expire();
                    TruthSpineProtocol.connectProject(project);
                }
            });
            CONNECTION_ACTIVE.put(project, notification);
            notification.notify(project);
        });
    }

    private static String html(String value) {
        return value.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }
}
