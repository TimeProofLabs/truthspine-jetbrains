package com.timeprooflabs.truthspine;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import org.jetbrains.annotations.NotNull;

public final class ConnectCurrentProjectAction extends AnAction {
    @Override
    public void actionPerformed(@NotNull AnActionEvent event) {
        if (event.getProject() != null) {
            TruthSpineProtocol.connectProject(event.getProject());
        }
    }

    @Override
    public void update(@NotNull AnActionEvent event) {
        event.getPresentation().setEnabled(event.getProject() != null);
    }
}
