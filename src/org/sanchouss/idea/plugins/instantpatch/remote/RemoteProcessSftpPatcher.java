package org.sanchouss.idea.plugins.instantpatch.remote;

import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.SftpException;
import org.sanchouss.idea.plugins.instantpatch.Checks;

import java.util.HashSet;
import java.util.List;


/**
 * Created by Alexander Perepelkin
 *
 * SFTP can not work with aliases like '~'. Need to provide full path
 */
public class RemoteProcessSftpPatcher {
    final String processDirectory;
    final RemoteClient remoteClient;

    public RemoteProcessSftpPatcher(RemoteClient remoteClient, String processDir) {
        Checks.checkEndsWithSlash(processDir);
        this.processDirectory = processDir;
        this.remoteClient = remoteClient;
    }

    public void cd(String dir) throws SftpException {
        remoteClient.arrangeSftpCommand(sftp -> sftp.cd(dir));
    }

    public void cd() throws SftpException {
        remoteClient.arrangeSftpCommand(sftp -> sftp.cd(processDirectory));
    }

    public void mkdir() throws SftpException {
        mkdir(processDirectory);
    }

    public void mkdir(String directory) throws SftpException {
        Checks.checkEndsWithSlash(directory);
        String subs[] = directory.split("/");

        remoteClient.arrangeSftpCommand(sftp -> makeSubDir(sftp, directory, 0, null));
    }

    public void mkdir(String directory, HashSet<String> existAlready) throws SftpException {
        remoteClient.arrangeSftpCommand(sftp -> {
            String pwd = sftp.pwd();
            System.out.println("Making directory " + directory + " at path " + processDirectory + " (pwd=" + pwd + ")");
            makeSubDir(sftp, directory, 0, existAlready);
        });
    }

    private void makeSubDir(ChannelSftp sftp, String directory, int index, HashSet<String> existAlready) throws SftpException {
        int delim = directory.indexOf('/', index);

        final String sub = (delim == -1) ? directory : directory.substring(0, delim);
        if (existAlready == null || !existAlready.contains(sub)) {
            System.out.println("Making directory " + sub);
            try {
                sftp.mkdir(sub);
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
            makeSubDir(sftp, directory, delim + 1, existAlready);
        }
    }

    public void chmod(int perm, String file) throws SftpException {
        System.out.println("Chmod " + perm + " on " + file);
        remoteClient.arrangeSftpCommand(sftp -> sftp.chmod(perm, file));
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
            remoteClient.arrangeSftpCommand(sftp -> sftp.put(src, dst, ChannelSftp.OVERWRITE));
        }
    }
}
