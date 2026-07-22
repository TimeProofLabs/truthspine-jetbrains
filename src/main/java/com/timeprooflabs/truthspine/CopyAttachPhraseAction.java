package com.timeprooflabs.truthspine;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.ide.CopyPasteManager;
import java.awt.datatransfer.StringSelection;
import org.jetbrains.annotations.NotNull;

public final class CopyAttachPhraseAction extends AnAction {
    @Override
    public void actionPerformed(@NotNull AnActionEvent event) {
        CopyPasteManager.getInstance().setContents(new StringSelection("Attach TruthSpine"));
        TruthSpineProtocol.notify(event.getProject(), "Attach TruthSpine copied.", com.intellij.notification.NotificationType.INFORMATION);
    }
}
