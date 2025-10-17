package org.sanchouss.idea.plugins.instantpatch.actions;

import com.intellij.openapi.actionSystem.AnAction;
import org.sanchouss.idea.plugins.instantpatch.remote.RemoteClient;

import java.util.List;

/**
 * Created by Alexander Perepelkin
 */
class CopyFilesToRemotePathActionGroup extends com.intellij.openapi.actionSystem.DefaultActionGroup {

    public CopyFilesToRemotePathActionGroup(RemoteClient remoteClient, List<String> remoteDirectories, String temporaryDirectory) {
        super("Copy specific files to remote dir", true);

        try {
            for (final String remoteDirectory : remoteDirectories) {
                AnAction action = new CopyFilesToRemotePathAction(remoteClient, remoteDirectory, temporaryDirectory);
                add(action);
            }
        } catch (Exception e) {
            add(new DumbAction("Exception while creating item. See stderr..."));
            e.printStackTrace();
        }
    }
}