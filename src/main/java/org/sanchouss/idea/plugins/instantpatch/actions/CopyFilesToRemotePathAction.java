package org.sanchouss.idea.plugins.instantpatch.actions;

import com.intellij.notification.Notification;
import com.intellij.notification.NotificationType;
import com.intellij.notification.Notifications;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.vfs.VirtualFile;
import org.sanchouss.idea.plugins.instantpatch.InstantPatchRemotePluginService;
import org.sanchouss.idea.plugins.instantpatch.remote.RemoteClient;
import org.sanchouss.idea.plugins.instantpatch.remote.RemoteProcessRunnerShell;
import org.sanchouss.idea.plugins.instantpatch.remote.RemoteProcessSftpPatcher;
import org.sanchouss.idea.plugins.instantpatch.util.ExceptionUtils;

import java.io.IOException;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import static org.sanchouss.idea.plugins.instantpatch.Checks.SLASH_LINUX_STYLE;


/**
 * Copies specific files chosen in IDE to the selected remote directory.
 * Firstly, uploads file to temporary remote directory; secondly, copies
 * from temporary to target remote directory in sudo mode (target may belong to root)
 *
 * Created by Alexander Perepelkin
 */
class CopyFilesToRemotePathAction extends AnAction {
    public static final String actionTitle = "Copying specific files to fixed remote";
    private final RemoteProcessSftpPatcher patcher;
    private final RemoteClient remoteClient;
    private final String remoteDirectory;
    private final RemoteProcessRunnerShell runner;
    private final String tmpDir;

    public CopyFilesToRemotePathAction(RemoteClient remoteClient, String remoteDirectory, String tempDirectory) {
        super(remoteDirectory);
        this.remoteClient = remoteClient;
        if (!remoteDirectory.endsWith(SLASH_LINUX_STYLE))
            remoteDirectory += SLASH_LINUX_STYLE;
        this.remoteDirectory = remoteDirectory;
        this.tmpDir = tempDirectory;
        this.patcher = remoteClient.createPatcher(tmpDir);
        this.runner = remoteClient.createRunnerShell("./", "");
    }

    public void actionPerformed(AnActionEvent e) {
        super.update(e);

        try {
            final VirtualFile[] files = e.getData(PlatformDataKeys.VIRTUAL_FILE_ARRAY);
            final LinkedList<VirtualFile> filesToCopy = new LinkedList<>(Arrays.asList(files));

            remoteClient.enqueue(new CopyFilesToRemotePathCommand(filesToCopy));
        } catch (Exception e1) {
            e1.printStackTrace();
            Notifications.Bus.notify(new Notification(InstantPatchRemotePluginService.notificationGroupId, actionTitle,
                ExceptionUtils.getStructuredErrorString(e1), NotificationType.ERROR));
        }
    }

    private class CopyFilesToRemotePathCommand implements Runnable {
        private final LinkedList<VirtualFile> filesToCopy;

        public CopyFilesToRemotePathCommand(LinkedList<VirtualFile> filesToCopy) {

            this.filesToCopy = filesToCopy;
        }

        public void run() {
            try {
                System.out.println("Got " + filesToCopy.size() + " files to copy chosen initially");
                patcher.mkdir();
                patcher.mkdir(remoteDirectory);
                int copied = 0, failed = 0;
                while (!filesToCopy.isEmpty()) {
                    final VirtualFile file = filesToCopy.removeFirst();

                    if (!file.isDirectory()) {
                        // upload to user's tmp dir, can not upload to root's dir via sftp
                        final String fileDir = file.getParent().getPath();
                        patcher.uploadFiles(fileDir, "./", List.of(file.getName()));
                        try {
                            final String tmpFile = tmpDir + file.getName(),
                                tmpFileLF = tmpDir + file.getName() + ".lf";
                            // truncate crlf to lf in uploaded file: file -> file.lf
                            runner.exec("tr -d '\\r' < " + tmpFile + " > " + tmpFileLF);
                            // todo: make backup of original once only - check for existing backup
                            final String targetFile = remoteDirectory + file.getName();
                            final String targetFileBAK = remoteDirectory + file.getName() + ".bak";
                            runner.exec("mv " + tmpFile + " " + targetFileBAK);
                            runner.exec("cp -f " + tmpFileLF + " " + targetFile);

                            // here try to cp twice: with standard perms and upgraded. todo - find cleaner way
                            // (sudo may require password to enter)
//                            runner.exec("sudo mv " + tmpFile + " " + targetFileBAK);
//                            runner.exec("sudo cp -f " + tmpFileLF + " " + targetFile);
                            ++copied;
                        } catch (IOException e1) {
                            e1.printStackTrace();
                            ++failed;
                        }
                    } else {
                        System.err.println("File is directory: " + file.getPath());
                    }
                }

                String msg = "Finished copying specific files to " + remoteDirectory + " \n" + copied + " copied, " + failed + " failed";
                System.out.println(msg);
                Notifications.Bus.notify(new Notification(InstantPatchRemotePluginService.notificationGroupId, actionTitle,
                    msg, NotificationType.INFORMATION));
            } catch (Exception e1) {
                e1.printStackTrace();
                Notifications.Bus.notify(new Notification(InstantPatchRemotePluginService.notificationGroupId, actionTitle,
                    e1.getMessage(), NotificationType.ERROR));
            }

        }
    }
}
