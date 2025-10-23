package org.sanchouss.idea.plugins.instantpatch.actions;

import com.intellij.notification.Notification;
import com.intellij.notification.NotificationType;
import com.intellij.notification.Notifications;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import org.sanchouss.idea.plugins.instantpatch.InstantPatchRemotePluginService;
import org.sanchouss.idea.plugins.instantpatch.remote.RemoteClient;
import org.sanchouss.idea.plugins.instantpatch.remote.RemoteClientLongRunningCommands;
import org.sanchouss.idea.plugins.instantpatch.remote.RemoteProcessRunnerShell;
import org.sanchouss.idea.plugins.instantpatch.settings.Process;
import org.sanchouss.idea.plugins.instantpatch.util.ExceptionUtils;

/**
 * Created by Alexander Perepelkin
 */
class RestartRemoteProcessAction extends AnAction {
    private static final String actionTitle = "Restarting remote processes";
    private final RemoteProcessRunnerShell runner;
    private final RemoteClient remoteClient;
    private final Process process;

    public RestartRemoteProcessAction(RemoteClient remoteClient, Process process) {
        super("Restart Remote Process");
        this.remoteClient = remoteClient;
        this.process = process;
        this.runner = new RemoteProcessRunnerShell(remoteClient, process.getProcessDirectory(), process.getProcessName());
    }

    @Override
    public void actionPerformed(AnActionEvent anActionEvent) {
        try {
            RemoteClientLongRunningCommands.getInstance().enqueue(remoteClient, new RestartRemoteProcessCommand());
        } catch (Exception e1) {
            e1.printStackTrace();
            Notifications.Bus.notify(new Notification(InstantPatchRemotePluginService.notificationGroupId, actionTitle,
                ExceptionUtils.getStructuredErrorString(e1), NotificationType.ERROR));
        }
    }

    private class RestartRemoteProcessCommand implements Runnable {
        @Override
        public void run() {
            try {
                runner.exec(process.getProcessStopCommand());
                runner.exec(process.getProcessStartCommand());
                Notifications.Bus.notify(new Notification(InstantPatchRemotePluginService.notificationGroupId, actionTitle,
                    runner.processDirectory + runner.processName + " restarted", NotificationType.INFORMATION));

            } catch (Exception e1) {
                e1.printStackTrace();
                Notifications.Bus.notify(new Notification(InstantPatchRemotePluginService.notificationGroupId, actionTitle,
                    ExceptionUtils.getStructuredErrorString(e1), NotificationType.ERROR));
            }
        }
    }
}
