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

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Proxies actual RemoteClientImpl,
 * <p>
 * Puts connections to the hosts in the queue in the separate thread.
 * After connection is established, delegating to actual RemoteClientImpl becomes possible
 * <p>
 * Created by Alexander Perepelkin
 */
public class RemoteClientProxy implements RemoteClient {
    private final AtomicReference<RemoteClientImpl> actual = new AtomicReference<>(null);
    private final AtomicReference<Exception> exception = new AtomicReference<>(null);
    private final String host;
    private final String user;
    private final int port;
    private final PluginSettingsCallback pluginSettingsCallback;
    private final ExecutorService executorService;

    public RemoteClientProxy(String host, String user, int port, PluginSettingsCallback pluginSettingsCallback, ExecutorService executorService)
        throws IOException, JSchException, SftpException, InterruptedException {
        this.host = host;
        this.user = user;
        this.port = port;
        this.pluginSettingsCallback = pluginSettingsCallback;
        this.executorService = executorService;
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

//    private void delayedConnect(RemoteAuth remoteAuth) {
//        executorService.submit(() -> connect(remoteAuth));
//    }

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
                "Connecting to host " + getHost() + " failed: " + e.getMessage(),
                NotificationType.ERROR));
        }
    }

    private RuntimeException getRuntimeException() {
        throw (exception.get() == null) ? new RuntimeException("Connection to host " + host + " is not established yet")
            : new RuntimeException("Error while establishing the connection: " + exception.get().getMessage(), exception.get());
    }

    public ChannelSftp getChannelSftp() {
        RemoteClientImpl client = actual.get();
        if (client != null) {
            return client.getChannelSftp();
        }
        throw getRuntimeException();
    }

    public ChannelShell getChannelShell() {
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

    /**
     * Should only be run from event dispatch thread!
     */
    @Override
    public void enqueue(Runnable command) {
        pluginSettingsCallback.getPluginSettings(true);
        executorService.submit(command);
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
            final PluginSettingsState pluginSettingsState = pluginSettingsCallback.getPluginSettings(false);
            reconnect(new RemoteAuth(pluginSettingsState.privateKeyFile, pluginSettingsState.passphrase));
        }

        try {
            sftpCommand.accept(this.getChannelSftp());
        } catch (SftpException e) {
            exception.set(e);
            throw new RuntimeException("SFTP error: " + e + "; " + errorMsg, e);
        }
    }

    @Override
    public void arrangeShellCommand(ShellCommand<ChannelShell> shellCommand, String errorMsg) {
        if (!isConnectedWell()) {
            final PluginSettingsState pluginSettingsState = pluginSettingsCallback.getPluginSettings(false);
            reconnect(new RemoteAuth(pluginSettingsState.privateKeyFile, pluginSettingsState.passphrase));
        }

        try {
            shellCommand.accept(this.getChannelShell());
        } catch (SftpException e) {
            exception.set(e);
            throw new RuntimeException("Shell error: " + e + "; " + errorMsg, e);
        }
    }

    public String getHost() {
        return host;
    }

}
