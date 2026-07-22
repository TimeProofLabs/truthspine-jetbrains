package com.timeprooflabs.truthspine;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import org.jetbrains.annotations.NotNull;

public final class OpenTruthSpineAction extends AnAction {
    @Override
    public void actionPerformed(@NotNull AnActionEvent event) {
        TruthSpineProtocol.openApp(event.getProject());
    }
}
