package org.sanchouss.idea.plugins.instantpatch;

import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSchException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * Establishes SSH connection for every command
 * Created by Alexander Perepelkin
 */
public class RemoteProcessRunnerExec {
    final String processDirectory;
    final RemoteClient remoteClient;

    public RemoteProcessRunnerExec(RemoteClient remoteClient, String processDir) {
        Checks.checkEndsWithSlash(processDir);
        this.processDirectory = processDir;
        this.remoteClient = remoteClient;
    }

    public void exec(String cmdToRun) throws IOException, JSchException, InterruptedException {
        ChannelExec channelExec = (ChannelExec) remoteClient.getSession().openChannel("exec");

        channelExec.setCommand(cmdToRun);
        channelExec.setInputStream(null);

        InputStream [] inputs = new InputStream [] {channelExec.getInputStream(), channelExec.getExtInputStream(), channelExec.getErrStream()};

        channelExec.connect();
        final long now = System.currentTimeMillis();
        boolean gotResult = false;
        while (!gotResult && System.currentTimeMillis() - now < 1000) {
            for (int i = 0; i < inputs.length; ++i)  {
                InputStream input = inputs[i];
                BufferedReader reader = new BufferedReader(new InputStreamReader(input));
                String line;
                StringBuilder builder = new StringBuilder();
                if (reader.ready()) {
                    while ((line = reader.readLine()) != null) {
                        builder.append(line).append("\n");
                    }
                    gotResult = true;
                    System.out.println("Got reply from input#" + i + ":\n" + builder.toString());
                    break;
                }
            }
            Thread.sleep(50);
        }

        int exitStatus = channelExec.getExitStatus();
        channelExec.disconnect();
        System.out.println("Executing command exit status: " + exitStatus);
    }

    public void restart() throws IOException, JSchException, InterruptedException {
        String cmdToRun = processDirectory + "reset.sh";
        exec(cmdToRun);
    }

    public void rmdir() throws IOException, JSchException, InterruptedException {
        String cmdToRun = "rm -rf " + processDirectory + "*";
        exec(cmdToRun);
    }

}
