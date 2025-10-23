package org.sanchouss.idea.plugins.instantpatch.remote;

import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.SftpException;
import org.sanchouss.idea.plugins.instantpatch.Checks;

import java.util.HashSet;
import java.util.List;

import static org.sanchouss.idea.plugins.instantpatch.Checks.SLASH_LINUX_STYLE;


/**
 * Wrapper over the remote client.
 * Provides api for ftp commands
 *
 * Created by Alexander Perepelkin
 *
 * SFTP can not work with aliases like '~'. Need to provide full path
 */
public class RemoteProcessSftpPatcher {
    final private String ftpDirectory;
    final private RemoteClient remoteClient;

    public RemoteProcessSftpPatcher(RemoteClient remoteClient, String ftpDir) {
        Checks.checkEndsWithSlash(ftpDir);
        this.ftpDirectory = ftpDir;
        this.remoteClient = remoteClient;
    }

    public void cd(String dir) throws SftpException {
        remoteClient.arrangeSftpCommand(chanSftp -> chanSftp.cd(dir), "Can not cd into " + dir);
    }

    public void cd() throws SftpException {
        remoteClient.arrangeSftpCommand(chanSftp -> chanSftp.cd(ftpDirectory), "Can not cd into " + ftpDirectory);
    }

    public void mkdir() throws SftpException {
        mkdir(ftpDirectory);
    }

    public void mkdir(String directory) throws SftpException {
        Checks.checkEndsWithSlash(directory);
        String subs[] = directory.split(SLASH_LINUX_STYLE);

        remoteClient.arrangeSftpCommand(sftp -> makeSubDir(sftp, directory, 0, null), "Can not mkdir " + directory);
    }

    public void mkdir(String directory, HashSet<String> existAlready) throws SftpException {
        remoteClient.arrangeSftpCommand(sftp -> {
            String pwd = sftp.pwd();
            System.out.println("Making directory " + directory + " at path " + ftpDirectory + " (pwd=" + pwd + ")");
            makeSubDir(sftp, directory, 0, existAlready);
        }, "Can not mkdir " + ftpDirectory);
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
        remoteClient.arrangeSftpCommand(chanSftp -> chanSftp.chmod(perm, file), "Can not chmod perm " + perm + " on file "
            + file);
    }

    public void uploadFiles(String fromLocalDirectory, String toRemoteDirectory, List<String> files) throws SftpException {
        for (String file: files) {
            if (fromLocalDirectory.isEmpty())
                fromLocalDirectory = ".";
            if (toRemoteDirectory.isEmpty())
                toRemoteDirectory = ".";

            if (!fromLocalDirectory.endsWith(SLASH_LINUX_STYLE))
                fromLocalDirectory += SLASH_LINUX_STYLE;
            if (!toRemoteDirectory.endsWith(SLASH_LINUX_STYLE))
                toRemoteDirectory += SLASH_LINUX_STYLE;


            String src = fromLocalDirectory + file, dst = ftpDirectory + toRemoteDirectory + file;
            System.out.println("Copying " + src + " -> " + dst);
            remoteClient.arrangeSftpCommand(chanSftp -> chanSftp.put(src, dst, ChannelSftp.OVERWRITE),
                "Can not copy file " + src + " -> " + dst);
        }
    }
}
