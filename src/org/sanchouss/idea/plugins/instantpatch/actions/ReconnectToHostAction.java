package org.sanchouss.idea.plugins.instantpatch.actions;

import com.intellij.openapi.ui.Messages;
import org.sanchouss.idea.plugins.instantpatch.InstantPatchRemotePluginRegistration;
import org.sanchouss.idea.plugins.instantpatch.RemoteAuth;
import org.sanchouss.idea.plugins.instantpatch.RemoteClientProxy;
import com.intellij.notification.Notification;
import com.intellij.notification.NotificationListener;
import com.intellij.notification.NotificationType;
import com.intellij.notification.Notifications;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import org.sanchouss.idea.plugins.instantpatch.settings.PluginSettings;

import static org.sanchouss.idea.plugins.instantpatch.InstantPatchRemotePluginRegistration.shortName;

/**
 * Created by Alexander Perepelkin
 */
class ReconnectToHostAction extends AnAction {
    private static final String actionTitle = "Reconnecting to remote host";
    private final PluginSettings pluginSettings;
    private final RemoteClientProxy remoteClientProxy;

    public ReconnectToHostAction(PluginSettings pluginSettings, RemoteClientProxy remoteClientProxy) {
        super("Reconnect to host");
        this.pluginSettings = pluginSettings;
        this.remoteClientProxy = remoteClientProxy;
    }

    void connect(boolean alwaysAskPassphrase) {
        if (pluginSettings.privateKeyFile == null) {
            Notifications.Bus.notify(new Notification(InstantPatchRemotePluginRegistration.notificationGroupId, actionTitle,
                    "Private key file path is not set up", NotificationType.WARNING, NotificationListener.URL_OPENING_LISTENER));
            return;
        }
        try {

            // when no connection yet or invalid connection
            if (alwaysAskPassphrase || pluginSettings.passphrase == null || pluginSettings.passphrase.isEmpty()) {
                pluginSettings.passphrase = Messages.showPasswordDialog("Enter passphrase for the private key", shortName);
            }
            System.out.println("Connecting to host " + remoteClientProxy.getHost());
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
        connect(true);
    }
}
