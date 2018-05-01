package org.sanchouss.idea.plugins.instantpatch.actions;

import com.intellij.notification.Notification;
import com.intellij.notification.NotificationListener;
import com.intellij.notification.NotificationType;
import com.intellij.notification.Notifications;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import org.sanchouss.idea.plugins.instantpatch.InstantPatchRemotePluginRegistration;
import org.sanchouss.idea.plugins.instantpatch.remote.RemoteAuth;
import org.sanchouss.idea.plugins.instantpatch.remote.RemoteClientProxy;
import org.sanchouss.idea.plugins.instantpatch.settings.PluginSettings;
import org.sanchouss.idea.plugins.instantpatch.settings.PluginSettingsCallback;

/**
 * Created by Alexander Perepelkin
 */
class ReconnectToHostAction extends AnAction {
    private static final String actionTitle = "Reconnecting to remote host";
    private final PluginSettingsCallback pluginSettingsCallback;
    private final RemoteClientProxy remoteClientProxy;

    public ReconnectToHostAction(PluginSettingsCallback pluginSettingsCallback, RemoteClientProxy remoteClientProxy) {
        super("Reconnect to host");
        this.pluginSettingsCallback = pluginSettingsCallback;
        this.remoteClientProxy = remoteClientProxy;
    }

    void connect() {

        try {
            System.out.println("Connecting to host " + remoteClientProxy.getHost());
            final PluginSettings pluginSettings = pluginSettingsCallback.getPluginSettings(true);
            remoteClientProxy.reconnect(new RemoteAuth(pluginSettings.privateKeyFile, pluginSettings.passphrase));
            Notifications.Bus.notify(new Notification(InstantPatchRemotePluginRegistration.notificationGroupId, actionTitle,
                    "Connecting to host " + remoteClientProxy.getHost() + " started", NotificationType.INFORMATION, NotificationListener.URL_OPENING_LISTENER));
        } catch (Exception e1) {
            e1.printStackTrace();
            Notifications.Bus.notify(new Notification(InstantPatchRemotePluginRegistration.notificationGroupId, actionTitle,
                    e1.toString(), NotificationType.ERROR, NotificationListener.URL_OPENING_LISTENER));
        }
    }

    @Override
    public void actionPerformed(AnActionEvent anActionEvent) {
        connect();
    }
}
