package org.sanchouss.idea.plugins.instantpatch;

import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.ChannelShell;
import com.jcraft.jsch.Session;

import java.io.PipedOutputStream;
import java.io.PrintStream;

/**
 * Created by Alexander Perepelkin
 */
public interface RemoteClient {
    ChannelSftp getCsftp();

    ChannelShell getChannelShell();

    PipedOutputStream getPipedOutputStreamCommandsToRemote();

    PrintStream getPipedOutputStreamCommandsToRemotePrinter();

    Session getSession();

    void disconnect();

    default RemoteProcessPatcher createPatcher(String processDir) {
        RemoteProcessPatcher rpp = new RemoteProcessPatcher(this, processDir);
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
}
