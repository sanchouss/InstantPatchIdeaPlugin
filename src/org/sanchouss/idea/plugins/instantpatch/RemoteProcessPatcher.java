package org.sanchouss.idea.plugins.instantpatch;

import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.SftpException;

import java.util.HashSet;
import java.util.List;


/**
 * Created by Alexander Perepelkin
 *
 * SFTP can not work with aliases like '~'. Need to provide full path
 */
public class RemoteProcessPatcher {
    final String processDirectory;
    final RemoteClient remoteClient;

    public RemoteProcessPatcher(RemoteClient remoteClient, String processDir) {
        Checks.checkEndsWithSlash(processDir);
        this.processDirectory = processDir;
        this.remoteClient = remoteClient;
    }

    public void cd(String processDir) throws SftpException {
        remoteClient.getCsftp().cd(processDir);
    }

    public void cd() throws SftpException {
        remoteClient.getCsftp().cd(processDirectory);
    }

    public void mkdir() throws SftpException {
        mkdir(processDirectory);
    }

    public void mkdir(String directory) throws SftpException {
        Checks.checkEndsWithSlash(directory);
        String subs[] = directory.split("/");

        makeSubDir(directory, 0, null);
    }

    public void mkdir(String directory, HashSet<String> existAlready) throws SftpException {
        String subs[] = directory.split("/");
        String pwd = remoteClient.getCsftp().pwd();
        System.out.println("Making directory " + directory + " at path " + processDirectory + " (pwd=" + pwd + ")");
        makeSubDir(directory, 0, existAlready);
    }

    private void makeSubDir(String directory, int index, HashSet<String> existAlready) throws SftpException {
        int delim = directory.indexOf('/', index);

        final String sub = (delim == -1) ? directory : directory.substring(0, delim);
        if (existAlready == null || !existAlready.contains(sub)) {
            System.out.println("Making directory " + sub);
            try {
                remoteClient.getCsftp().mkdir(sub);
                if (existAlready != null)
                    existAlready.add(sub);
            } catch (SftpException e) {
                // still may exist remotely
                if (e.id != ChannelSftp.SSH_FX_FAILURE) {
                    System.err.println(sub + " may exist already: got err#" + e.id);
                    throw e;
                }
                if (existAlready != null)
                    existAlready.add(sub);
            }
        }

        if (delim != -1 && delim != directory.length()-1) {
            makeSubDir(directory, delim + 1, existAlready);
        }
    }

    public void chmod(int perm, String file) throws SftpException {
        System.out.println("Chmod " + perm + " on " + file);
        remoteClient.getCsftp().chmod(perm, file);
    }

    public void uploadFiles(String fromLocalDirectory, String toRemoteDirectory, List<String> files) throws SftpException {
        for (String file: files) {
            if (fromLocalDirectory.isEmpty())
                fromLocalDirectory = ".";
            if (toRemoteDirectory.isEmpty())
                toRemoteDirectory = ".";

            if (!fromLocalDirectory.endsWith("/"))
                fromLocalDirectory += "/";
            if (!toRemoteDirectory.endsWith("/"))
                toRemoteDirectory += "/";


            String src = fromLocalDirectory + file, dst = processDirectory + toRemoteDirectory + file;
            System.out.println("Copying " + src + " -> " + dst);
            remoteClient.getCsftp().put(src, dst, ChannelSftp.OVERWRITE);
        }
    }
}
