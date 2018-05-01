package org.sanchouss.idea.plugins.instantpatch.actions;

import com.intellij.notification.Notification;
import com.intellij.notification.NotificationListener;
import com.intellij.notification.NotificationType;
import com.intellij.notification.Notifications;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.Constraints;
import org.sanchouss.idea.plugins.instantpatch.ConfigSerializer;
import org.sanchouss.idea.plugins.instantpatch.InstantPatchRemotePluginRegistration;
import org.sanchouss.idea.plugins.instantpatch.settings.Configuration;
import org.sanchouss.idea.plugins.instantpatch.settings.Host;
import org.sanchouss.idea.plugins.instantpatch.settings.PluginSettings;
import org.sanchouss.idea.plugins.instantpatch.settings.PluginSettingsCallback;
import org.sanchouss.idea.plugins.instantpatch.util.ExceptionUtils;

import javax.xml.bind.JAXBException;

/**
 * Created by Alexander Perepelkin
 */
public class RemoteOperationRootGroup extends com.intellij.openapi.actionSystem.DefaultActionGroup {
    private String configPath;
    private static final AnAction dumbAction = new DumbAction("Error initializing this plugin. See stderr...");

    private final AnAction reloadAction = new ReloadConfigAction(this);
    private final PluginSettings pluginSettings;

    public RemoteOperationRootGroup(PluginSettings pluginSettings) {

        super("Copy/Restart Remote...", true);
        this.pluginSettings = pluginSettings;

        composeActions();
        // TODO: after sleep time pipes are closed - need to reopen them
    }

    void composeActions() {
        removeAll();

        configPath = pluginSettings.configFile;

        try {
            if (configPath == null || configPath.isEmpty()) {
                add(new DumbAction("Plugin is not set up"), Constraints.FIRST);
            } else {
                Configuration config = readConfig();
                addActions(config);
            }
        } catch (Exception e) {
            add(dumbAction, Constraints.FIRST);
        } finally {
            add(new PluginSettingsActionGroup(pluginSettings));
            add(reloadAction, Constraints.LAST);
        }
    }

    private void addActions(Configuration config) {
        System.out.println("Config " + configPath + " is read");
        Notifications.Bus.notify(new Notification(InstantPatchRemotePluginRegistration.notificationGroupId, "Loading " + InstantPatchRemotePluginRegistration.shortName,
            "Config " + configPath + " is read", NotificationType.INFORMATION, NotificationListener.URL_OPENING_LISTENER));

        for (final Host host : config.getHosts()) {
            AnAction action = new HostActionGroup(host, new PluginSettingsCallback(pluginSettings));
            add(action);
//            Notifications.Bus.notify(new Notification(InstantPatchRemotePluginRegistration.notificationGroupId, "Loading " + InstantPatchRemotePluginRegistration.shortName,
//                "Host action " + host.getHostname() + " is created", NotificationType.INFORMATION, NotificationListener.URL_OPENING_LISTENER));
        }
        System.out.println("Action items (" + config.getHosts().size() + ") are created");
//        Notifications.Bus.notify(new Notification(InstantPatchRemotePluginRegistration.notificationGroupId, "Loading " + InstantPatchRemotePluginRegistration.shortName,
//            "Action items (" + config.getHosts().size() + ") are created", NotificationType.INFORMATION, NotificationListener.URL_OPENING_LISTENER));
    }

    private Configuration readConfig() {
            System.out.println("Reading config " + configPath + " ...");
//            Notifications.Bus.notify(new Notification(InstantPatchRemotePluginRegistration.notificationGroupId, "Loading " + InstantPatchRemotePluginRegistration.shortName,
//                    "Reading config " + configPath + " ...", NotificationType.INFORMATION, NotificationListener.URL_OPENING_LISTENER));
            try {
                return ConfigSerializer.read(configPath);
            } catch (JAXBException e) {
                e.printStackTrace();
                Notifications.Bus.notify(new Notification(InstantPatchRemotePluginRegistration.notificationGroupId, "Loading " + InstantPatchRemotePluginRegistration.shortName,
                        "Error reading config " + configPath + ": " + ExceptionUtils.getStructuredErrorString(e), NotificationType.ERROR, NotificationListener.URL_OPENING_LISTENER));
                throw new RuntimeException(e);
            }
    }

    @Override
    public void beforeActionPerformedUpdate(AnActionEvent e) {
        super.beforeActionPerformedUpdate(e);
    }

    @Override
    public void actionPerformed(AnActionEvent anActionEvent) {
        System.out.print("CopyClassesToRemoteGroup");
    }

}
