package org.sanchouss.idea.plugins.instantpatch.actions;

import org.sanchouss.idea.plugins.instantpatch.RemoteClient;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.Constraints;
import org.sanchouss.idea.plugins.instantpatch.settings.Process;

/**
 * Created by Alexander Perepelkin
 */
class ProcessActionGroup extends com.intellij.openapi.actionSystem.DefaultActionGroup  {

    public ProcessActionGroup(Process process, RemoteClient remoteClient) {
        super(process.getProcessName(), true);

        final CopyClassesToRemoteAction copyClasses = new CopyClassesToRemoteAction(remoteClient, process);
        add(copyClasses);
        final RestartRemoteProcessAction restart = new RestartRemoteProcessAction(remoteClient, process);
        add(restart);
        add(new CleanRemotePatchesAction(remoteClient, process));
        if (copyClasses != null) {
            add(new CopyClassesAndRestartRemoteProcessAction(copyClasses, restart), Constraints.FIRST);
        }
        final CopyFilesToRemotePathActionGroup copyFiles = new CopyFilesToRemotePathActionGroup(remoteClient,
            process.getRemoteDirectories(), process.getTemporaryDirectory());
        add(copyFiles);
    }

    @Override
    public void actionPerformed(AnActionEvent anActionEvent) {

    }
}
