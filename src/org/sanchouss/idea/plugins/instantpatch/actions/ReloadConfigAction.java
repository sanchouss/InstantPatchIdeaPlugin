package org.sanchouss.idea.plugins.instantpatch.actions;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;

/**
 * Created by Alexander Perepelkin
 */
class ReloadConfigAction extends AnAction {
    private final RemoteOperationRootGroup rootGroup;

    public ReloadConfigAction(RemoteOperationRootGroup rootGroup) {
        super("Reload plugin config");
        this.rootGroup = rootGroup;
    }

    @Override
    public void actionPerformed(AnActionEvent anActionEvent) {
        rootGroup.composeActions();
    }
}