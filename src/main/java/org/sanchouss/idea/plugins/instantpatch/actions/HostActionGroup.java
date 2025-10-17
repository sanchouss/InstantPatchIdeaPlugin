package org.sanchouss.idea.plugins.instantpatch.actions;

import com.intellij.openapi.actionSystem.AnAction;
import org.sanchouss.idea.plugins.instantpatch.Utils;
import org.sanchouss.idea.plugins.instantpatch.remote.RemoteClientProxy;
import org.sanchouss.idea.plugins.instantpatch.settings.Host;
import org.sanchouss.idea.plugins.instantpatch.settings.PluginSettingsCallback;
import org.sanchouss.idea.plugins.instantpatch.settings.Process;

import java.util.Optional;

/**
 * Created by Alexander Perepelkin
 */
class HostActionGroup extends com.intellij.openapi.actionSystem.DefaultActionGroup {
    private final Host host;
    private final PluginSettingsCallback pluginSettingsCallback;
    private final RemoteClientProxy remoteClientProxy;
    private final Optional<ReconnectToHostAction> reconnectAction;

    public HostActionGroup(Host host, PluginSettingsCallback pluginSettingsCallback) {
        super(host.getHostname(), true);
        this.host = host;
        this.pluginSettingsCallback = pluginSettingsCallback;

        RemoteClientProxy proxy = null;
        ReconnectToHostAction reconnect = null;
        try {
            proxy = new RemoteClientProxy(host.getHostname(), host.getUsername(), 22, pluginSettingsCallback,
                    Utils.getExecutorService());

            for (final Process process : host.getProcesses()) {
                AnAction action = new ProcessActionGroup(process, proxy);
                add(action);
            }

            reconnect = new ReconnectToHostAction(pluginSettingsCallback, proxy);
            add(reconnect);
        } catch (Exception e) {
            add(new DumbAction(host.getHostname() + ": Exception while creating host menu group item. See stderr..."));
            System.err.println(e);
        } finally {
            this.remoteClientProxy = proxy;
            this.reconnectAction = Optional.ofNullable(reconnect);
        }
    }
}