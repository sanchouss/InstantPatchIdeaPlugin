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

    // send simple string command
    void sendShellCommand(String cmdToRun, int waitForReplyForMillis);

    // send command as lambda, may include few commands and logic
    void arrangeSftpCommand(SftpCommand<ChannelSftp> sftpCommand, String errorMsg);

    // send command as lambda, may include few commands and logic
    void arrangeShellCommand(ShellCommand<ChannelShell> shellCommand, String errorMsg);

    // wrappers over the client
    default RemoteProcessSftpPatcher createPatcher(String ftpDir) {
        RemoteProcessSftpPatcher rpp = new RemoteProcessSftpPatcher(this, ftpDir);
        return rpp;
    }

    default RemoteProcessRunnerShell createRunnerShell(String processDir, String processName) {
        RemoteProcessRunnerShell rpr = new RemoteProcessRunnerShell(this, processDir, processName);
        return rpr;
    }

    void disconnect();
}
