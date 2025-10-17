package org.sanchouss.idea.plugins.instantpatch.settings;

import com.intellij.openapi.ui.Messages;

import static org.sanchouss.idea.plugins.instantpatch.InstantPatchRemotePluginService.shortName;

/**
 * Created by Alexander Perepelkin
 */
public class PluginSettingsCallback {
    final PluginSettingsState pluginSettingsState;

    public PluginSettingsCallback(PluginSettingsState pluginSettingsState) {
        this.pluginSettingsState = pluginSettingsState;
    }

    public void clearPassphrase() {
        pluginSettingsState.passphrase = null;
    }

    public PluginSettingsState getPluginSettings(boolean mayAskPassphrase) {
        if (pluginSettingsState.privateKeyFile == null) {
            throw new RuntimeException("Private key file path is not set up");
        }
        // when no connection yet or invalid connection
        // todo: handle invalid passphrase yet
        if (pluginSettingsState.passphrase == null) {
            if (mayAskPassphrase) {
                pluginSettingsState.passphrase =
                        Messages.showPasswordDialog(
//                        Messages.showInputDialog(
                                "Enter passphrase for the private key" + "\n" + "(leave empty if no passphrase)",
                                shortName
//                                , Messages.getQuestionIcon()
                        );
            } else {
                throw new RuntimeException("Passphrase was not set up");
            }
        }

        return pluginSettingsState;
    }
}
