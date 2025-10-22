package org.sanchouss.idea.plugins.instantpatch.remote;

import org.sanchouss.idea.plugins.instantpatch.Utils;
import org.sanchouss.idea.plugins.instantpatch.settings.PluginSettingsCallback;

import java.util.concurrent.ExecutorService;

public class RemoteClientLongRunningCommands {
    private static final RemoteClientLongRunningCommands instance = new RemoteClientLongRunningCommands();
    private final ExecutorService executorService = Utils.getExecutorService();
    private PluginSettingsCallback pluginSettingsCallback;

    public static RemoteClientLongRunningCommands getInstance() {
        return instance;
    }

    /**
     * Should only be run from event dispatch thread (EDT)!
     */
    public void enqueue(RemoteClient remoteClient, Runnable command) {
        // make sure in EDT that passphrase is provided before running long network task
        pluginSettingsCallback.getPluginSettings(true);
        executorService.submit(command);
    }

    public void setPluginSettingsCallback(PluginSettingsCallback pluginSettingsCallback) {
        this.pluginSettingsCallback = pluginSettingsCallback;
    }
}
