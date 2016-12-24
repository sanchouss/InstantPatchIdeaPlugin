package org.sanchouss.idea.plugins.instantpatch.settings;

import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.openapi.components.StoragePathMacros;
import org.jetbrains.annotations.Nullable;

/**
 * Created by Alexander Perepelkin
 */
public class PluginSettings {

    public String configFile;
    public String privateKeyFile;

    @com.intellij.util.xmlb.annotations.Transient
    public String passphrase;
}
