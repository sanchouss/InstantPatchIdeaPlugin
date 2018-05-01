package org.sanchouss.idea.plugins.instantpatch.remote;

import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.ChannelShell;
import com.jcraft.jsch.Session;

/**
 * Establishes session to remote host and create sftp/shell channels.
 *
 * Created by Alexander Perepelkin
 */
public interface RemoteClient {
    Session getSession();

    void arrangeSftpCommand(SftpCommand<ChannelSftp> sftpCommand);

    void arrangeShellCommand(ShellCommand<ChannelShell> shellCommand);

    void disconnect();

    default RemoteProcessSftpPatcher createPatcher(String processDir) {
        RemoteProcessSftpPatcher rpp = new RemoteProcessSftpPatcher(this, processDir);
        return rpp;
    }

    default RemoteProcessRunnerExec createRunnerExec(String processDir) {
        RemoteProcessRunnerExec rpr = new RemoteProcessRunnerExec(this, processDir);
        return rpr;
    }

    default RemoteProcessRunnerShell createRunnerShell(String processDir, String processName) {
        RemoteProcessRunnerShell rpr = new RemoteProcessRunnerShell(this, processDir, processName);
        return rpr;
    }

    void enqueue(Runnable command);

    void sendShellCommand(String cmdToRun);

    void sendShellCommand(String cmdToRun, int waitForReplyForMillis);
}
