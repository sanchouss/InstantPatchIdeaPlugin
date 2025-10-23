package org.sanchouss.idea.plugins.instantpatch.actions;

import com.intellij.notification.Notification;
import com.intellij.notification.NotificationType;
import com.intellij.notification.Notifications;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.Constraints;
import org.sanchouss.idea.plugins.instantpatch.ConfigSerializer;
import org.sanchouss.idea.plugins.instantpatch.InstantPatchRemotePluginService;
import org.sanchouss.idea.plugins.instantpatch.remote.RemoteClientLongRunningCommands;
import org.sanchouss.idea.plugins.instantpatch.settings.Configuration;
import org.sanchouss.idea.plugins.instantpatch.settings.Host;
import org.sanchouss.idea.plugins.instantpatch.settings.PluginSettingsCallback;
import org.sanchouss.idea.plugins.instantpatch.settings.PluginSettingsState;
import org.sanchouss.idea.plugins.instantpatch.util.ExceptionUtils;

import javax.xml.bind.JAXBException;

/**
 * Created by Alexander Perepelkin
 */
public class RemoteOperationRootGroup extends com.intellij.openapi.actionSystem.DefaultActionGroup {
    private String configPath;
    private static final AnAction dumbAction = new DumbAction("Error initializing this plugin. See stderr...");

    private final AnAction reloadAction = new ReloadConfigAction(this);
    private final PluginSettingsState pluginSettingsState;

    public RemoteOperationRootGroup(PluginSettingsState pluginSettingsState) {

        super("Copy/Restart Remote...", true);
        this.pluginSettingsState = pluginSettingsState;

        composeActions();
        // TODO: after sleep time pipes are closed - need to reopen them
    }

    void composeActions() {
        // todo: disconnect all the remote clients assigned to the menu items and previously used
        removeAll();

        configPath = pluginSettingsState.configFile;

        try {
            if (configPath == null || configPath.isEmpty()) {
                add(new DumbAction("Plugin has no config"), Constraints.FIRST);
            } else {
                Configuration config = readConfig();
                addActions(config);
            }
        } catch (Exception e) {
            add(dumbAction, Constraints.FIRST);
        } finally {
            add(new PluginSettingsActionGroup(this, pluginSettingsState));
            add(reloadAction, Constraints.LAST);
        }
    }

    private void addActions(Configuration config) {
        System.out.println("Config " + configPath + " is read");
        Notifications.Bus.notify(new Notification(InstantPatchRemotePluginService.notificationGroupId, "Loading " + InstantPatchRemotePluginService.shortName,
            "Config " + configPath + " is read", NotificationType.INFORMATION));

        PluginSettingsCallback pluginSettingsCallback = new PluginSettingsCallback(pluginSettingsState);
        RemoteClientLongRunningCommands.getInstance().setPluginSettingsCallback(pluginSettingsCallback);
        for (final Host host : config.getHosts()) {
            AnAction action = new HostActionGroup(host, pluginSettingsState);
            add(action);
//            Notifications.Bus.notify(new Notification(InstantPatchRemotePluginService.notificationGroupId, "Loading " + InstantPatchRemotePluginService.shortName,
//                "Host action " + host.getHostname() + " is created", NotificationType.INFORMATION));
        }
        System.out.println("Action items (" + config.getHosts().size() + ") are created");
//        Notifications.Bus.notify(new Notification(InstantPatchRemotePluginService.notificationGroupId, "Loading " + InstantPatchRemotePluginService.shortName,
//            "Action items (" + config.getHosts().size() + ") are created", NotificationType.INFORMATION));
    }

    private Configuration readConfig() {
            System.out.println("Reading config " + configPath + " ...");
//            Notifications.Bus.notify(new Notification(InstantPatchRemotePluginService.notificationGroupId, "Loading " + InstantPatchRemotePluginService.shortName,
//                    "Reading config " + configPath + " ...", NotificationType.INFORMATION));
            try {
                return ConfigSerializer.read(configPath);
            } catch (JAXBException e) {
                e.printStackTrace();
                Notifications.Bus.notify(new Notification(InstantPatchRemotePluginService.notificationGroupId, "Loading " + InstantPatchRemotePluginService.shortName,
                        "Error reading config " + configPath + ": " + ExceptionUtils.getStructuredErrorString(e), NotificationType.ERROR));
                throw new RuntimeException(e);
            }
    }
}
