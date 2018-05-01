package org.sanchouss.idea.plugins.instantpatch.actions;

import org.sanchouss.idea.plugins.instantpatch.InstantPatchRemotePluginRegistration;
import org.sanchouss.idea.plugins.instantpatch.remote.RemoteClient;
import org.sanchouss.idea.plugins.instantpatch.remote.RemoteProcessRunnerShell;
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
class RestartRemoteProcessAction extends AnAction {
    private static final String actionTitle = "Restarting remote processes";
    private final RemoteProcessRunnerShell runner;
    private final RemoteClient remoteClient;

    public RestartRemoteProcessAction(RemoteClient remoteClient, Process process) {
        super("Restart Remote Process");
        this.remoteClient = remoteClient;
        this.runner = remoteClient.createRunnerShell(process.getProcessDirectory(), process.getProcessName());
    }

    @Override
    public void actionPerformed(AnActionEvent anActionEvent) {
        super.update(anActionEvent);
        try {
            remoteClient.enqueue(new RestartRemoteProcessCommand());
        } catch (Exception e1) {
            e1.printStackTrace();
            Notifications.Bus.notify(new Notification(InstantPatchRemotePluginRegistration.notificationGroupId, actionTitle,
                e1.toString(), NotificationType.ERROR, NotificationListener.URL_OPENING_LISTENER));
        }
    }

    private class RestartRemoteProcessCommand implements Runnable {
        @Override
        public void run() {
            try {
                runner.exec("sudo " + runner.processDirectory + runner.processName + " stop");
                runner.exec("sudo " + runner.processDirectory + runner.processName + " start");
                Notifications.Bus.notify(new Notification(InstantPatchRemotePluginRegistration.notificationGroupId, actionTitle,
                    runner.processDirectory + runner.processName + " restarted", NotificationType.INFORMATION, NotificationListener.URL_OPENING_LISTENER));

            } catch (Exception e1) {
                e1.printStackTrace();
                Notifications.Bus.notify(new Notification(InstantPatchRemotePluginRegistration.notificationGroupId, actionTitle,
                    e1.toString(), NotificationType.ERROR, NotificationListener.URL_OPENING_LISTENER));
            }
        }
    }
}
