package org.sanchouss.idea.plugins.instantpatch.actions;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.fileChooser.FileChooser;
import com.intellij.openapi.fileChooser.FileChooserDescriptor;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import org.sanchouss.idea.plugins.instantpatch.settings.PluginSettings;

import java.io.File;

/**
 * Created by Alexander Perepelkin
 */
public class PluginSettingsActionGroup extends com.intellij.openapi.actionSystem.DefaultActionGroup {
    private final PluginSettings pluginSettings;
    private final FileChooserDescriptor descriptor = new FileChooserDescriptor(true, false, false, false,false, false);

    public PluginSettingsActionGroup(PluginSettings pluginSettings) {
        super("Settings", true);
        this.pluginSettings = pluginSettings;
        reload();
    }

    private void reload() {
        removeAll();
        add(new PluginSettingsConfigFilePathActionGroup());
        add(new PluginSettingsPrivateKeyFilePathActionGroup());
    }


    class PluginSettingsConfigFilePathActionGroup extends com.intellij.openapi.actionSystem.DefaultActionGroup {
        PluginSettingsConfigFilePathActionGroup() {
            super("Config file", true);

            final String configFile = pluginSettings.configFile;
            add(new DumbAction((configFile == null) ? "None" : configFile));
            add(new PluginSettingsConfigFilePathChooseAction());

            //todo: reload config file and recreate actions
        }
    }

    class PluginSettingsConfigFilePathChooseAction extends AnAction {

        public PluginSettingsConfigFilePathChooseAction() {
            super("Choose file...");
        }

        @Override
        public void actionPerformed(AnActionEvent anActionEvent) {
            final VirtualFile toSelect = StringUtil.isEmpty(pluginSettings.configFile) ? null :
                LocalFileSystem.getInstance().findFileByIoFile(new File(pluginSettings.configFile));
            FileChooser.chooseFile(descriptor, null, toSelect, virtualFile -> {
                pluginSettings.configFile = virtualFile.getCanonicalPath();
                reload();
            });
        }
    }

    class PluginSettingsPrivateKeyFilePathActionGroup extends com.intellij.openapi.actionSystem.DefaultActionGroup {
        PluginSettingsPrivateKeyFilePathActionGroup() {
            super("Private key file", true);

            final String privateKeyFile = pluginSettings.privateKeyFile;
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
            final VirtualFile toSelect = StringUtil.isEmpty(pluginSettings.privateKeyFile) ? null :
                LocalFileSystem.getInstance().findFileByIoFile(new File(pluginSettings.privateKeyFile));
            FileChooser.chooseFile(descriptor, null, toSelect, virtualFile -> {
                pluginSettings.privateKeyFile = virtualFile.getCanonicalPath();
                reload();
            });
        }
    }
}
