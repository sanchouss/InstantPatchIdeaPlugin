package org.sanchouss.idea.plugins.instantpatch.remote;

import com.jcraft.jsch.JSchException;
import org.sanchouss.idea.plugins.instantpatch.Checks;

import java.io.IOException;
import java.util.HashSet;

/**
 * Wrapper over the remote client.
 * Provides api for shell commands
 *
 * Uses same 'shell' channel for all commands.
 *
 * Created by Alexander Perepelkin
 */
public class RemoteProcessRunnerShell {
    public final String processDirectory;
    public final String processName;
    public final RemoteClient remoteClient;

    public RemoteProcessRunnerShell(RemoteClient remoteClient, String processDir, String processName) {
        Checks.checkEndsWithSlash(processDir);
        this.processDirectory = processDir;
        this.processName = processName;
        this.remoteClient = remoteClient;
    }

    public void exec(String cmdToRun) throws IOException {

        remoteClient.arrangeShellCommand(chanShell -> {
            System.out.println("EXEC: " + "Executing command: " + cmdToRun);
            // send the cmd to channel, do not wait for the response here
            remoteClient.sendShellCommand(cmdToRun, 3000);
        }, "Can not execute shell command " + cmdToRun);
/*
        remoteClient.channelShellInputs = new InputStream[] {channelShell.getInputStream(), channelShell.getExtInputStream()};

        final long now = System.currentTimeMillis();
        boolean gotResult = false, continueWaiting=true;
        while (continueWaiting && System.currentTimeMillis() - now < 1000) {
            for (int i = 0; i < remoteClient.channelShellInputs.length; ++i)  {
                InputStream input = remoteClient.channelShellInputs[i];
                BufferedInputStream reader = new BufferedInputStream(input);
                String line;
                StringBuilder builder = new StringBuilder();
                byte buf[] = new byte[1024];

                if (reader.available() > 0) {
                    // BufferedReader reader; reader.readLine() will not work...
                    int read = reader.read(buf);
                    System.out.println("HOST: " + new String(buf, 0, read, "UTF-8"));
                    gotResult = true;
                    if (read == -1) {
                        System.out.println("HOST: " + "End of remote stream");
                        break;
                    }
                } else {
                    if (gotResult) {
                        continueWaiting = false;
                        break;
                    }
                }
            }
            Thread.sleep(50);
        }
        System.out.println("HOST: " + "reply took " + (System.currentTimeMillis()-now) + "ms");
*/
    }


    public void mkdir(String directory) throws IOException, JSchException, InterruptedException {
        Checks.checkEndsWithSlash(directory);
        String fullpath = processDirectory + directory;
        System.out.println("Making directory " + fullpath);
        String cmdToRun = "mkdir -p " + fullpath;
        exec(cmdToRun);
    }

    public void mkdir(String directory, HashSet<String> existAlreadyDirs) throws IOException {
        String fullpath = processDirectory + directory;
        System.out.println("Making directory " + fullpath);
        String cmdToRun = "mkdir -p " + fullpath;
        exec(cmdToRun);
    }

    public void rmdir() throws IOException, JSchException, InterruptedException {
        String fullpath = processDirectory + "*";
        System.out.println("Removing directory " + fullpath);
        String cmdToRun = "rm -rf " + fullpath;
        exec(cmdToRun);
    }

    public void echo(String line) throws IOException, JSchException, InterruptedException {
        String cmdToRun = "echo " + line;
        exec(cmdToRun);
    }

}
