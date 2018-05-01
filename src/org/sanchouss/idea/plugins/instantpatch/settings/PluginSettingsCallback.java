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

    public PluginSettings getPluginSettings(boolean alwaysAskPassphrase) {
        if (pluginSettings.privateKeyFile == null) {
            throw new RuntimeException("Private key file path is not set up");
        }
        // when no connection yet or invalid connection
        if (alwaysAskPassphrase || pluginSettings.passphrase == null || pluginSettings.passphrase.isEmpty()) {
            pluginSettings.passphrase = Messages.showPasswordDialog("Enter passphrase for the private key", shortName);
        }

        return pluginSettings;
    }
}
