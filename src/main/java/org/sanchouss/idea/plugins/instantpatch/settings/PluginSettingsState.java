package org.sanchouss.idea.plugins.instantpatch.settings;

/**
 * Created by Alexander Perepelkin
 */
public class PluginSettingsState {

    public String configFile;
    public String privateKeyFile;

    @com.intellij.util.xmlb.annotations.Transient
    public String passphrase;
}
