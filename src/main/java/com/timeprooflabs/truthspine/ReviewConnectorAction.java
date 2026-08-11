package com.timeprooflabs.truthspine;

import com.intellij.ide.BrowserUtil;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import org.jetbrains.annotations.NotNull;

public final class ReviewConnectorAction extends AnAction {
    private static final String REVIEW_URL = "https://plugins.jetbrains.com/plugin/33056-truthspine/reviews";

    @Override
    public void actionPerformed(@NotNull AnActionEvent event) {
        BrowserUtil.browse(REVIEW_URL);
    }
}
