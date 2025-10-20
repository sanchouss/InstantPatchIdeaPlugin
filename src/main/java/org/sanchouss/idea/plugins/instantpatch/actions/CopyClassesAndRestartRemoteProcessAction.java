package org.sanchouss.idea.plugins.instantpatch.actions;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;

/**
 * Created by Alexander Perepelkin
 */
class CopyClassesAndRestartRemoteProcessAction extends AnAction {
    private final CopyClassesToRemoteAction copy;
    private final RestartRemoteProcessAction restart;

    public CopyClassesAndRestartRemoteProcessAction(CopyClassesToRemoteAction copy, RestartRemoteProcessAction restart) {
        super("Copy Classes And Restart Remote Process");
        this.copy = copy;
        this.restart = restart;
    }

    @Override
    public void actionPerformed(AnActionEvent anActionEvent) {
        try {
            copy.actionPerformed(anActionEvent);
            restart.actionPerformed(anActionEvent);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
