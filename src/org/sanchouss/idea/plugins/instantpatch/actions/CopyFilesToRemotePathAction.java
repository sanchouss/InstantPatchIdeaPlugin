package org.sanchouss.idea.plugins.instantpatch.actions;

import org.sanchouss.idea.plugins.instantpatch.InstantPatchRemotePluginRegistration;
import org.sanchouss.idea.plugins.instantpatch.RemoteClient;
import org.sanchouss.idea.plugins.instantpatch.RemoteProcessPatcher;
import org.sanchouss.idea.plugins.instantpatch.RemoteProcessRunnerShell;
import com.google.common.collect.Lists;
import com.intellij.notification.Notification;
import com.intellij.notification.NotificationListener;
import com.intellij.notification.NotificationType;
import com.intellij.notification.Notifications;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.vfs.VirtualFile;

import java.io.IOException;
import java.util.Arrays;
import java.util.LinkedList;


/**
 * Copies specific files chosen in IDE to the selected remote directory.
 * Firstly, uploads file to temporary remote directory; secondly, copies
 * from temporary to target remote directory in sudo mode (target may belong to root)
 *
 * Created by Alexander Perepelkin
 */
class CopyFilesToRemotePathAction extends AnAction {
    public static final String actionTitle = "Copying specific files to fixed remote";
    private final RemoteProcessPatcher patcher;
    private final String remoteDirectory;
    private final RemoteProcessRunnerShell runner;
    private final String tmpDir;

    public CopyFilesToRemotePathAction(RemoteClient remoteClient, String remoteDirectory, String tempDirectory) {
        super(remoteDirectory);
        this.remoteDirectory = remoteDirectory;
        this.tmpDir = tempDirectory;
        this.patcher = remoteClient.createPatcher(tmpDir);
        this.runner = remoteClient.createRunnerShell("./", "");
    }

    public void actionPerformed(AnActionEvent e) {
        super.update(e);

        try {
            final VirtualFile[] files = e.getData(PlatformDataKeys.VIRTUAL_FILE_ARRAY);
            final LinkedList<VirtualFile> filesToCopy = Lists.newLinkedList(Arrays.asList(files));
            System.out.println("Got " + filesToCopy.size() + " items to copy chosen initially");
            int copied = 0, failed = 0;
            while (!filesToCopy.isEmpty()) {
                final VirtualFile file = filesToCopy.removeFirst();

                if (!file.isDirectory()) {
                    // upload to user's tmp dir, can not upload to root's dir via sftp
                    final String fileDir = file.getParent().getPath();
                    patcher.uploadFiles(fileDir, "./", Lists.newArrayList(file.getName()));
                    try {
                        final String tmpFile = tmpDir + file.getName(),
                            tmpFileLF = tmpDir + file.getName() + ".lf";
                        // truncate crlf to lf in uploaded file
                        runner.exec("tr -d '\\r' < " + tmpFile + " > " + tmpFileLF);
                        // todo: make backup of original once only - check for existing backup
                        final String targetFile = remoteDirectory + file.getName();
                        runner.exec("sudo cp " + targetFile + " " + targetFile + ".bak");
                        runner.exec("sudo cp -f " + tmpFileLF + " " + targetFile);
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
            Notifications.Bus.notify(new Notification(InstantPatchRemotePluginRegistration.notificationGroupId, actionTitle,
                    msg, NotificationType.INFORMATION, NotificationListener.URL_OPENING_LISTENER));

        } catch (Exception e1) {
            e1.printStackTrace();
            Notifications.Bus.notify(new Notification(InstantPatchRemotePluginRegistration.notificationGroupId, actionTitle,
                e1.toString(), NotificationType.ERROR, NotificationListener.URL_OPENING_LISTENER));
        }
    }
}