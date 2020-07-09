package org.sanchouss.idea.plugins.instantpatch.actions;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;

/**
 * Created by Alexander Perepelkin
 */
class DumbAction extends AnAction {
    public DumbAction() {
        super();
    }

    public DumbAction(String text) {
        super(text);
    }

    @Override
    public void actionPerformed(AnActionEvent anActionEvent) {
        System.err.println(getTemplatePresentation().getText());
    }
}