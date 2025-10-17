package org.sanchouss.idea.plugins.instantpatch.actions;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.fileChooser.FileChooser;
import com.intellij.openapi.fileChooser.FileChooserDescriptor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import org.sanchouss.idea.plugins.instantpatch.settings.PluginSettingsState;

import java.io.File;

/**
 * Created by Alexander Perepelkin
 */
public class PluginSettingsActionGroup extends com.intellij.openapi.actionSystem.DefaultActionGroup {
    private final RemoteOperationRootGroup rootGroup;
    private final PluginSettingsState pluginSettingsState;
    private final FileChooserDescriptor descriptor = new FileChooserDescriptor(true, false, false, false, false, false);

    public PluginSettingsActionGroup(RemoteOperationRootGroup rootGroup, PluginSettingsState pluginSettingsState) {
        super("Settings", true);
        this.rootGroup = rootGroup;
        this.pluginSettingsState = pluginSettingsState;

        // menu text might change after choosing actions, then it's easier to recreate items
        reload();
    }

    private void reload() {
        removeAll();
        add(new PluginSettingsConfigFilePathActionGroup(rootGroup));
        add(new PluginSettingsPrivateKeyFilePathActionGroup());
    }


    class PluginSettingsConfigFilePathActionGroup extends com.intellij.openapi.actionSystem.DefaultActionGroup {

        PluginSettingsConfigFilePathActionGroup(RemoteOperationRootGroup rootGroup) {
            super("Config file", true);

            final String configFile = pluginSettingsState.configFile;
            add(new DumbAction((configFile == null) ? "None" : configFile));
            add(new PluginSettingsConfigFilePathChooseAction(rootGroup));
        }
    }

    class PluginSettingsConfigFilePathChooseAction extends AnAction {
        private final RemoteOperationRootGroup rootGroup;

        public PluginSettingsConfigFilePathChooseAction(RemoteOperationRootGroup rootGroup) {
            super("Choose file...");
            this.rootGroup = rootGroup;
        }

        @Override
        public void actionPerformed(AnActionEvent anActionEvent) {
            final VirtualFile toSelect = StringUtil.isEmpty(pluginSettingsState.configFile) ? null :
                    LocalFileSystem.getInstance().findFileByIoFile(new File(pluginSettingsState.configFile));
            VirtualFile[] virtualFiles = FileChooser.chooseFiles(descriptor, null, toSelect);
            if (virtualFiles.length == 1) {
                pluginSettingsState.configFile = virtualFiles[0].getPath();
//                reload();
                // recreate all actions with new config file (new hosts, processes, etc)
                rootGroup.composeActions();
            } else if (virtualFiles.length != 0) {
                Messages.showErrorDialog((Project) null, "Single config file should be chosen", "Invalid File");
            }
        }
    }

    class PluginSettingsPrivateKeyFilePathActionGroup extends com.intellij.openapi.actionSystem.DefaultActionGroup {
        PluginSettingsPrivateKeyFilePathActionGroup() {
            super("Private key file", true);

            final String privateKeyFile = pluginSettingsState.privateKeyFile;
            add(new DumbAction((privateKeyFile == null) ? "None" : privateKeyFile));
            add(new PluginSettingsPrivateKeyFilePathChooseAction());
        }
    }

    class PluginSettingsPrivateKeyFilePathChooseAction extends AnAction {

        public PluginSettingsPrivateKeyFilePathChooseAction() {
            super("Choose file...");
        }

        @Override
        public void actionPerformed(AnActionEvent anActionEvent) {
            final VirtualFile toSelect = StringUtil.isEmpty(pluginSettingsState.privateKeyFile) ? null :
                    LocalFileSystem.getInstance().findFileByIoFile(new File(pluginSettingsState.privateKeyFile));

            VirtualFile[] virtualFiles = FileChooser.chooseFiles(descriptor, null, toSelect);
            if (virtualFiles.length == 1) {
                pluginSettingsState.privateKeyFile = virtualFiles[0].getPath();
                reload();
            } else if (virtualFiles.length != 0) {
                Messages.showErrorDialog((Project) null, "Single key file should be chosen", "Invalid File");
            }
        }
    }
}
