package org.sanchouss.idea.plugins.instantpatch.remote;

import com.jcraft.jsch.*;
import io.netty.util.internal.StringUtil;

import java.io.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Establishes session to remote host and opens sftp and shell channels.
 *
 * Created by Alexander Perepelkin
 */
public class RemoteClientImpl implements RemoteClient {
    private final String host, user;
    private final int port;
    private final JSch jsch;

    private final Session session;
    private final ChannelSftp channelSftp;
    private final ChannelShell channelShell;

    private final PipedOutputStream pipedOutputStreamCommandsToRemote;
    private final PrintStream pipedOutputStreamCommandsToRemotePrinter;
    private final ThreadInputStreamReader inputShellReader;
    private final ThreadInputStreamReader errorShellReader;

    private final AtomicReference<WaitForRemoteReply> waitForReply = new AtomicReference<>();

    private static class WaitForRemoteReply {
        private final CountDownLatch latch;
        private final int waitForMillis;

        private WaitForRemoteReply(CountDownLatch latch, int waitForMillis) {
            this.latch = latch;
            this.waitForMillis = waitForMillis;
        }
    }

    public RemoteClientImpl(String host, String user, int port, RemoteAuth remoteAuth) throws IOException, JSchException, SftpException, InterruptedException {
        this.host = host;
        this.user = user;
        this.port = port;

        this.jsch = new JSch();
        jsch.addIdentity(remoteAuth.privateKeyFile, StringUtil.isNullOrEmpty(remoteAuth.passphrase) ?
            null : remoteAuth.passphrase);

        session = jsch.getSession(user, host, port);
        System.out.println("session created.");

        java.util.Properties config = new java.util.Properties();
        config.put("StrictHostKeyChecking", "no");
        config.put("PreferredAuthentications",
                "publickey,keyboard-interactive,password");
        session.setConfig(config);
        session.connect();
        System.out.println("session connected.....");

        {
            Channel channel = session.openChannel("sftp");
//            channel.setInputStream(System.in);
//            channel.setOutputStream(System.out);
            channel.connect();
            channelSftp = (ChannelSftp) channel;
            System.out.println("ftp channel connected....");
        }

        /*
            few tests

        String pwd = channelSftp.pwd();
        Vector<ChannelSftp.LsEntry> ls = channelSftp.ls(".");
        System.out.println(pwd);
        System.out.println(ls);

        */
//            String fileName = "test.txt";
//            channelSftp.put(fileName, "./in/");
        System.out.println("ftp connected");

        {
            Channel channel = session.openChannel("shell");
            // Enable agent-forwarding.

//        channelShell.setAgentForwarding(true);
//        channelShell.setPtyType("vt102");
//
/*
            /// possible option to read output from the remote server.
            /// WARN: calling channelShell.getInputStream() will erase this call: channel.setOutputStream(pipedOutputStream);

            {
                PipedOutputStream pipedOutputStream = new PipedOutputStream();
                final PipedInputStream pipedInputStreamOutputFromRemote = new PipedInputStream(pipedOutputStream);
                channel.setOutputStream(pipedOutputStream);
                channel.setExtOutputStream(pipedOutputStream);

                new Thread("Thread Reader from Remote Host Shell") {
                    @Override
                    public void run() {
                        try {
                            byte buf[] = new byte[1024];
                            BufferedReader reader = new BufferedReader(new InputStreamReader(pipedInputStreamOutputFromRemote));
                            while (!isInterrupted()) {
                                // BufferedReader reader; reader.readLine() will not work...
                                int read = pipedInputStreamOutputFromRemote.read(buf);
                                System.out.println("SSHL: " + new String(buf, 0, read, "UTF-8"));

                                if (read == -1) {
                                    System.out.println("End of remote stream");
                                    break;
                                }
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                        System.out.println(getName() + " finished");

                    }
                }.start();
            }
*/
            {
                pipedOutputStreamCommandsToRemote = new PipedOutputStream();
                PipedInputStream pipedInputStreamCommandsToRemote = new PipedInputStream(pipedOutputStreamCommandsToRemote);
                pipedOutputStreamCommandsToRemotePrinter = new PrintStream(pipedOutputStreamCommandsToRemote);

                channel.setInputStream(pipedInputStreamCommandsToRemote);
            }

            channelShell = (ChannelShell) channel;
            InputStream[] channelShellInputs = new InputStream[]{channelShell.getInputStream(), channelShell.getExtInputStream()};

            inputShellReader = new ThreadInputStreamReader("Thread getInputStream Reader", "INPT", channelShellInputs[0]);
            errorShellReader = new ThreadInputStreamReader("Thread getExtInputStream Reader", "INPE", channelShellInputs[1]);
            inputShellReader.start();
            errorShellReader.start();

            channelShell.connect(1000);
            System.out.println("shell connected");
        }
    }

    public ChannelSftp getChannelSftp() {
        return channelSftp;
    }

    public ChannelShell getChannelShell() {
        return channelShell;
    }

    @Override
    public Session getSession() {
        return session;
    }

    private class ThreadInputStreamReader extends Thread {
        private final String prefix;
        private final InputStream inputStream;
        public ThreadInputStreamReader(String name, String prefix, InputStream inputStream) {
            super(name);
            this.prefix = prefix;
            this.inputStream = inputStream;
        }
        @Override
        public void run() {
            try {
                while (!isInterrupted()) {
                    byte buf[] = new byte[1024];

                    // BufferedReader reader, reader.readLine() will not work...
                    int read = inputStream.read(buf);

                    final WaitForRemoteReply waitForRemoteReply = waitForReply.getAndSet(null);
                    if (waitForRemoteReply != null) {
                        waitForRemoteReply.latch.countDown();
                    }

                    if (read == -1) {
                        System.out.println("End of remote stream");
                        break;
                    }
                    String rcvd = new String(buf, 0, read, "UTF-8");
                    System.out.println(prefix + ": " + rcvd);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void disconnect() {
        channelSftp.disconnect();
        channelShell.disconnect();
        session.disconnect();
        System.out.println("disconnected");

    }

    @Override
    public void enqueue(Runnable command) {
        command.run();
    }

    @Override
    public void sendShellCommand(String cmdToRun) {
        pipedOutputStreamCommandsToRemotePrinter.println(cmdToRun);
        pipedOutputStreamCommandsToRemotePrinter.flush();
    }

    @Override
    public void sendShellCommand(String cmdToRun, int waitForReplyForMillis) {
        final WaitForRemoteReply waitForRemoteReply = new WaitForRemoteReply(new CountDownLatch(1), waitForReplyForMillis);
        waitForReply.set(waitForRemoteReply);
        sendShellCommand(cmdToRun);
        try {
            waitForRemoteReply.latch.await(waitForReplyForMillis, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void arrangeSftpCommand(SftpCommand<ChannelSftp> sftpCommand) {
        try {
            sftpCommand.accept(channelSftp);
        } catch (SftpException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void arrangeShellCommand(ShellCommand<ChannelShell> shellCommand) {
        try {
            shellCommand.accept(channelShell);
        } catch (SftpException e) {
            throw new RuntimeException(e);
        }
    }
}
