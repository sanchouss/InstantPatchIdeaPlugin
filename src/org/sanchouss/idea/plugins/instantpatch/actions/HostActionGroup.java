package org.sanchouss.idea.plugins.instantpatch.actions;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DataContext;
import org.sanchouss.idea.plugins.instantpatch.RemoteClientProxy;
import org.sanchouss.idea.plugins.instantpatch.Utils;
import org.sanchouss.idea.plugins.instantpatch.settings.Host;
import org.sanchouss.idea.plugins.instantpatch.settings.PluginSettings;
import org.sanchouss.idea.plugins.instantpatch.settings.Process;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by Alexander Perepelkin
 *
 */
class HostActionGroup extends com.intellij.openapi.actionSystem.DefaultActionGroup {
    private final Host host;
    private final PluginSettings pluginSettings;
    private final RemoteClientProxy remoteClientProxy;
    private final Optional<ReconnectToHostAction> reconnectAction;
    private AtomicBoolean wasChosenOnceAlready = new AtomicBoolean(false);

    public HostActionGroup(Host host, PluginSettings pluginSettings) {
        super(host.getHostname(), true);
        this.host = host;
        this.pluginSettings = pluginSettings;

        RemoteClientProxy proxy = null;
        ReconnectToHostAction reconnect = null;
        try {
            proxy = new RemoteClientProxy(host.getHostname(), host.getUsername(), 22,
                    Utils.getExecutorService());

            for (final Process process : host.getProcesses()) {
                AnAction action = new ProcessActionGroup(process, proxy);
                add(action);
            }

            reconnect = new ReconnectToHostAction(pluginSettings, proxy);
            add(reconnect);
        } catch (Exception e) {
            add(new DumbAction(host.getHostname() + ": Exception while creating host item. See stderr..."));
        } finally {
            this.remoteClientProxy = proxy;
            this.reconnectAction = Optional.ofNullable(reconnect);
        }
    }

    /**
     * @return true if {@link #actionPerformed(AnActionEvent)} should be called
     */
    @Override
    public boolean canBePerformed(DataContext context) {
        return true;
    }

    @Override
    public void actionPerformed(AnActionEvent anActionEvent) {
        // todo: move (re)connection to the endpoint action
        if (wasChosenOnceAlready.compareAndSet(false, true)) {
            reconnectAction.ifPresent(r -> r.connect(false));
        }
    }
}