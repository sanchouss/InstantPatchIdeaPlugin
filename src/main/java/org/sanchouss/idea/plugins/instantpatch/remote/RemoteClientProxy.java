package org.sanchouss.idea.plugins.instantpatch.remote;

import com.intellij.notification.Notification;
import com.intellij.notification.NotificationListener;
import com.intellij.notification.NotificationType;
import com.intellij.notification.Notifications;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.ChannelShell;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.SftpException;
import org.sanchouss.idea.plugins.instantpatch.InstantPatchRemotePluginService;
import org.sanchouss.idea.plugins.instantpatch.settings.PluginSettingsState;
import org.sanchouss.idea.plugins.instantpatch.settings.PluginSettingsCallback;
import org.sanchouss.idea.plugins.instantpatch.util.ExceptionUtils;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Proxies actual RemoteClientImpl. Substitutes real clients in menu items, allowing to mimic remote client,
 * but postponing connecting to remote host until it is requested.
 * After connection is established, delegating calls to actual RemoteClientImpl becomes possible.
 * Tries to reconnect if connection is broken.
 * <p>
 * Created by Alexander Perepelkin
 */
public class RemoteClientProxy implements RemoteClient {
    private final AtomicReference<RemoteClientImpl> actual = new AtomicReference<>(null);
    private final AtomicReference<Exception> exception = new AtomicReference<>(null);
    private final String host;
    private final String user;
    private final int port;
    // PPK, passphrase may change due to user input
    private final PluginSettingsState pluginSettingsState;

    public RemoteClientProxy(String host, String user, int port, PluginSettingsState pluginSettingsState)
            throws IOException, JSchException, SftpException, InterruptedException {
        this.host = host;
        this.user = user;
        this.port = port;
        this.pluginSettingsState = pluginSettingsState;
    }

    public RemoteClientImpl getActualRemoteClient() {
        return actual.get();
    }

    public void reconnect(RemoteAuth remoteAuth) {
        RemoteClientImpl client = actual.get();
        if (client != null) {
            client.disconnect();
        }
        exception.set(null);
        actual.set(null);
        connect(remoteAuth);
    }

    private void connect(RemoteAuth remoteAuth) {
        try {
            Notifications.Bus.notify(new Notification(InstantPatchRemotePluginService.notificationGroupId, "Connecting",
                    "Connecting to host " + getHost() + " ...", NotificationType.INFORMATION));
            actual.set(new RemoteClientImpl(host, user, port, remoteAuth));
            Notifications.Bus.notify(new Notification(InstantPatchRemotePluginService.notificationGroupId, "Connecting",
                    "Connected to host " + getHost() + " ok", NotificationType.INFORMATION));
        } catch (Exception e) {
            exception.set(e);
            e.printStackTrace();
            Notifications.Bus.notify(new Notification(InstantPatchRemotePluginService.notificationGroupId, "Connecting",
                    "Connecting to host " + getHost() + " failed: " + ExceptionUtils.getStructuredErrorString(e),
                    NotificationType.ERROR));
        }
    }

    private RuntimeException getRuntimeException() {
        return (exception.get() == null)
                ? new RuntimeException("Connection to host " + host + " is not established yet")
                : new RuntimeException("Error while establishing the connection: " + exception.get().getMessage(), exception.get());
    }

    private ChannelSftp getChannelSftp() {
        RemoteClientImpl client = actual.get();
        if (client != null) {
            return client.getChannelSftp();
        }
        throw getRuntimeException();
    }

    private ChannelShell getChannelShell() {
        RemoteClientImpl client = actual.get();
        if (client != null) {
            return client.getChannelShell();
        }
        throw getRuntimeException();
    }

    @Override
    public Session getSession() {
        RemoteClientImpl client = actual.get();
        if (client != null) {
            return client.getSession();
        }
        throw getRuntimeException();
    }

    @Override
    public void disconnect() {
        RemoteClientImpl client = actual.get();
        if (client != null) {
            client.disconnect();
        } else {
            throw getRuntimeException();
        }
    }

    @Override
    public void sendShellCommand(String cmdToRun) {
        RemoteClientImpl client = actual.get();
        if (client != null) {
            client.sendShellCommand(cmdToRun);
        } else {
            throw getRuntimeException();
        }
    }

    @Override
    public void sendShellCommand(String cmdToRun, int waitForReplyForMillis) {
        RemoteClientImpl client = actual.get();
        if (client != null) {
            client.sendShellCommand(cmdToRun, waitForReplyForMillis);
        } else {
            throw getRuntimeException();
        }
    }

    private boolean isConnectedWell() {
        return actual.get() != null && exception.get() == null;
    }

    @Override
    public void arrangeSftpCommand(SftpCommand<ChannelSftp> sftpCommand, String errorMsg) {
        if (!isConnectedWell()) {
            reconnect(new RemoteAuth(pluginSettingsState.privateKeyFile, pluginSettingsState.passphrase));
        }

        try {
            sftpCommand.accept(this.getChannelSftp());
        } catch (SftpException e) {
            exception.set(e);
            throw new RuntimeException("SFTP error: " + e.getMessage() + "; " + errorMsg, e);
        }
    }

    @Override
    public void arrangeShellCommand(ShellCommand<ChannelShell> shellCommand, String errorMsg) {
        if (!isConnectedWell()) {
            reconnect(new RemoteAuth(pluginSettingsState.privateKeyFile, pluginSettingsState.passphrase));
        }

        try {
            shellCommand.accept(this.getChannelShell());
        } catch (Exception e) {
            exception.set(e);
            throw new RuntimeException("Shell error: " + e.getMessage() + "; " + errorMsg, e);
        }
    }

    public String getHost() {
        return host;
    }

}
