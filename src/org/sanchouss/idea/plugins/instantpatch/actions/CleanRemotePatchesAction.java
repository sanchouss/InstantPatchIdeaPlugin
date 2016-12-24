package org.sanchouss.idea.plugins.instantpatch.actions;

import org.sanchouss.idea.plugins.instantpatch.InstantPatchRemotePluginRegistration;
import org.sanchouss.idea.plugins.instantpatch.RemoteClient;
import org.sanchouss.idea.plugins.instantpatch.RemoteProcessRunnerShell;
import org.sanchouss.idea.plugins.instantpatch.settings.Process;
import com.intellij.notification.Notification;
import com.intellij.notification.NotificationListener;
import com.intellij.notification.NotificationType;
import com.intellij.notification.Notifications;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;

/**
 * Created by Alexander Perepelkin
 */
class CleanRemotePatchesAction extends AnAction {
    private static final String actionTitle = "Cleaning remote directory";
    private final RemoteProcessRunnerShell runner;

    public CleanRemotePatchesAction(RemoteClient remoteClient, Process process) {
        super("Clean Remote Patch Directory");
        this.runner = remoteClient.createRunnerShell(process.getClassFilesDirectory(), process.getProcessName());
    }

    @Override
    public void actionPerformed(AnActionEvent anActionEvent) {
        super.update(anActionEvent);
        try {
            runner.rmdir();
            Notifications.Bus.notify(new Notification(InstantPatchRemotePluginRegistration.notificationGroupId, actionTitle,
                runner.processDirectory + " cleaned", NotificationType.INFORMATION, NotificationListener.URL_OPENING_LISTENER));

        } catch (Exception e1) {
            e1.printStackTrace();
            Notifications.Bus.notify(new Notification(InstantPatchRemotePluginRegistration.notificationGroupId, actionTitle,
                e1.toString(), NotificationType.ERROR, NotificationListener.URL_OPENING_LISTENER));
        }
    }
}
