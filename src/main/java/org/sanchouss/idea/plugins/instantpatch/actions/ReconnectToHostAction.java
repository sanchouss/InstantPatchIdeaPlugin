package org.sanchouss.idea.plugins.instantpatch.actions;

import com.intellij.notification.Notification;
import com.intellij.notification.NotificationType;
import com.intellij.notification.Notifications;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import org.sanchouss.idea.plugins.instantpatch.InstantPatchRemotePluginService;
import org.sanchouss.idea.plugins.instantpatch.remote.RemoteAuth;
import org.sanchouss.idea.plugins.instantpatch.remote.RemoteClientProxy;
import org.sanchouss.idea.plugins.instantpatch.settings.PluginSettingsCallback;
import org.sanchouss.idea.plugins.instantpatch.settings.PluginSettingsState;
import org.sanchouss.idea.plugins.instantpatch.util.ExceptionUtils;

/**
 * Created by Alexander Perepelkin
 */
class ReconnectToHostAction extends AnAction {
    private static final String actionTitle = "Reconnecting to remote host";
    private final PluginSettingsState pluginSettingsState;
    private final RemoteClientProxy remoteClientProxy;

    public ReconnectToHostAction(PluginSettingsState pluginSettingsState, RemoteClientProxy remoteClientProxy) {
        super("Reconnect to host");
        this.pluginSettingsState = pluginSettingsState;
        this.remoteClientProxy = remoteClientProxy;
    }

    void reconnect() {

        try {
            System.out.println("Reconnecting to host " + remoteClientProxy.getHost());
            PluginSettingsCallback pluginSettingsCallback = new PluginSettingsCallback(pluginSettingsState);
            pluginSettingsCallback.clearPassphrase();
            pluginSettingsCallback.getPluginSettings(true);

            // possibly long operation as well, can be enqueued instead of running in EDT
            remoteClientProxy.reconnect(new RemoteAuth(pluginSettingsState.privateKeyFile, pluginSettingsState.passphrase));
        } catch (Exception e1) {
            e1.printStackTrace();
            Notifications.Bus.notify(new Notification(InstantPatchRemotePluginService.notificationGroupId, actionTitle,
                    e1.getMessage(), NotificationType.ERROR));
        }
    }

    @Override
    public void actionPerformed(AnActionEvent anActionEvent) {
        reconnect();
    }
}
