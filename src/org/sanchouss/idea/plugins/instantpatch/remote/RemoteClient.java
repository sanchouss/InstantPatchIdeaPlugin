package org.sanchouss.idea.plugins.instantpatch.remote;

import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.ChannelShell;
import com.jcraft.jsch.Session;

import java.io.PipedOutputStream;
import java.io.PrintStream;

/**
 * Establishes session to remote host and create sftp/shell channels.
 *
 * Created by Alexander Perepelkin
 */
public interface RemoteClient {
    Session getSession();

    void arrangeSftpCommand(SftpCommand<ChannelSftp> sftpCommand);

    void arrangeShellCommand(ShellCommand<ChannelShell> shellCommand);

    PipedOutputStream getPipedOutputStreamCommandsToRemote();

    PrintStream getPipedOutputStreamCommandsToRemotePrinter();

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

    PrintStream getChannelShellToRemotePrinter();

    void enqueue(Runnable command);
}
