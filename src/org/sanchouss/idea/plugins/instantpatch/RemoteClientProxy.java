package org.sanchouss.idea.plugins.instantpatch;

import com.intellij.notification.Notification;
import com.intellij.notification.NotificationListener;
import com.intellij.notification.NotificationType;
import com.intellij.notification.Notifications;
import com.jcraft.jsch.*;

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
 * After connection is established, delegating to actual RemoteClientImpl is made possible
 * <p>
 * Created by Alexander Perepelkin
 */
public class RemoteClientProxy implements RemoteClient {
    private final AtomicReference<RemoteClientImpl> actual = new AtomicReference<>(null);
    private final AtomicReference<Exception> exception = new AtomicReference<>(null);
    private final String host;
    private final String user;
    private final int port;
    private final ExecutorService executorService;

    public RemoteClientProxy(String host, String user, int port, ExecutorService executorService)
        throws IOException, JSchException, SftpException, InterruptedException {
        this.host = host;
        this.user = user;
        this.port = port;
        this.executorService = executorService;
    }

    public boolean isConnected() {
        // todo: handle case after established connection gets broken
        return actual.get() != null && exception.get() == null;
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
        delayedConnect(remoteAuth);
    }

    private void delayedConnect(RemoteAuth remoteAuth) {
        executorService.submit(() -> {
            try {
                actual.set(new RemoteClientImpl(host, user, port, remoteAuth));
            } catch (Exception e) {
                exception.set(e);
                e.printStackTrace();
                Notifications.Bus.notify(new Notification(InstantPatchRemotePluginRegistration.notificationGroupId, "Loading " + InstantPatchRemotePluginRegistration.shortName,
                    host + ": Exception while creating host item:" + e.toString() + Arrays.toString(e.getStackTrace()),
                    NotificationType.ERROR, NotificationListener.URL_OPENING_LISTENER));
            }
        });
    }

    private RuntimeException getRuntimeException() {
        throw (exception.get() == null) ? new RuntimeException("Connection is not established yet")
            : new RuntimeException("Error while establishing the connection: ", exception.get());
    }

    @Override
    public ChannelSftp getCsftp() {
        RemoteClientImpl client = actual.get();
        if (client != null) {
            return client.getCsftp();
        }
        throw getRuntimeException();
    }

    @Override
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

    public String getHost() {
        return host;
    }

}
