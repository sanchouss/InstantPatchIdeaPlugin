package org.sanchouss.idea.plugins.instantpatch.remote;

import com.intellij.notification.Notification;
import com.intellij.notification.NotificationListener;
import com.intellij.notification.NotificationType;
import com.intellij.notification.Notifications;
import com.jcraft.jsch.*;
import org.sanchouss.idea.plugins.instantpatch.InstantPatchRemotePluginRegistration;
import org.sanchouss.idea.plugins.instantpatch.settings.PluginSettings;
import org.sanchouss.idea.plugins.instantpatch.settings.PluginSettingsCallback;

import java.io.IOException;
import java.io.PipedOutputStream;
import java.io.PrintStream;
import java.util.Arrays;
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
            Notifications.Bus.notify(new Notification(InstantPatchRemotePluginRegistration.notificationGroupId, "Connecting",
                "Connecting to host " + getHost() + " started", NotificationType.INFORMATION, NotificationListener.URL_OPENING_LISTENER));
            actual.set(new RemoteClientImpl(host, user, port, remoteAuth));
            Notifications.Bus.notify(new Notification(InstantPatchRemotePluginRegistration.notificationGroupId, "Connecting",
                "Connected to host " + getHost() + " ok", NotificationType.INFORMATION, NotificationListener.URL_OPENING_LISTENER));
        } catch (Exception e) {
            exception.set(e);
            e.printStackTrace();
            Notifications.Bus.notify(new Notification(InstantPatchRemotePluginRegistration.notificationGroupId, "Loading " + InstantPatchRemotePluginRegistration.shortName,
                host + ": Exception while connecting to host:" + e.toString() + Arrays.toString(e.getStackTrace()),
                NotificationType.ERROR, NotificationListener.URL_OPENING_LISTENER));
        }
    }

    private RuntimeException getRuntimeException() {
        throw (exception.get() == null) ? new RuntimeException("Connection is not established yet")
            : new RuntimeException("Error while establishing the connection: ", exception.get());
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
    public PipedOutputStream getPipedOutputStreamCommandsToRemote() {
        RemoteClientImpl client = actual.get();
        if (client != null) {
            return client.getPipedOutputStreamCommandsToRemote();
        }
        throw getRuntimeException();
    }

    @Override
    public PrintStream getPipedOutputStreamCommandsToRemotePrinter() {
        RemoteClientImpl client = actual.get();
        if (client != null) {
            return client.getPipedOutputStreamCommandsToRemotePrinter();
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
    public PrintStream getChannelShellToRemotePrinter() {
        RemoteClientImpl client = actual.get();
        if (client != null) {
            return client.getChannelShellToRemotePrinter();
        }
        throw getRuntimeException();
    }

    @Override
    public void enqueue(Runnable command) {

        pluginSettingsCallback.getPluginSettings(false);
        executorService.submit(command);
    }

    private boolean isConnectedWell() {
        return actual.get() != null && exception.get() == null;
    }

    @Override
    public void arrangeSftpCommand(SftpCommand<ChannelSftp> sftpCommand) {
        // todo: handle notifications after bad connection
        if (!isConnectedWell()) {
            final PluginSettings pluginSettings = pluginSettingsCallback.getPluginSettings(false);
            reconnect(new RemoteAuth(pluginSettings.privateKeyFile, pluginSettings.passphrase));
        }
//        executorService.submit(() -> {
            try {
                sftpCommand.accept(this.getChannelSftp());
            } catch (SftpException e) {
                exception.set(e);
            }
//        });
    }

    @Override
    public void arrangeShellCommand(ShellCommand<ChannelShell> shellCommand) {
        if (!isConnectedWell()) {
            final PluginSettings pluginSettings = pluginSettingsCallback.getPluginSettings(false);
            reconnect(new RemoteAuth(pluginSettings.privateKeyFile, pluginSettings.passphrase));
        }

//        executorService.submit(() -> {
            try {
                shellCommand.accept(this.getChannelShell());
            } catch (SftpException e) {
                exception.set(e);
            }
//        });
    }

    public String getHost() {
        return host;
    }

}
