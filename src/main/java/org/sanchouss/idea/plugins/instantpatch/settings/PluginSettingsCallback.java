package org.sanchouss.idea.plugins.instantpatch.settings;

import com.intellij.openapi.ui.Messages;

import static org.sanchouss.idea.plugins.instantpatch.InstantPatchRemotePluginRegistration.shortName;

/**
 * Created by Alexander Perepelkin
 */
public class PluginSettingsCallback {
    final PluginSettings pluginSettings;

    public PluginSettingsCallback(PluginSettings pluginSettings) {
        this.pluginSettings = pluginSettings;
    }

    public void clearPassphrase() {
        pluginSettings.passphrase = null;
    }

    public PluginSettings getPluginSettings(boolean mayAskPassphrase) {
        if (pluginSettings.privateKeyFile == null) {
            throw new RuntimeException("Private key file path is not set up");
        }
        // when no connection yet or invalid connection
        if (pluginSettings.passphrase == null) {
            if (mayAskPassphrase) {
                pluginSettings.passphrase = Messages.showPasswordDialog(
                    "Enter passphrase for the private key" + "\n" + "(leave empty if no passphrase)", shortName);
            } else {
                throw new RuntimeException("Passphrase was not set up");
            }
        }

        return pluginSettings;
    }
}
