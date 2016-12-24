package org.sanchouss.idea.plugins.instantpatch;

import com.intellij.openapi.ui.Messages;
import org.sanchouss.idea.plugins.instantpatch.actions.RemoteOperationRootGroup;
import org.sanchouss.idea.plugins.instantpatch.settings.PluginSettings;
import com.intellij.notification.Notification;
import com.intellij.notification.NotificationListener;
import com.intellij.notification.NotificationType;
import com.intellij.notification.Notifications;
import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.components.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;

/**
 * Created by Alexander Perepelkin
 *
 */
// todo: remove platform-dependant path separator
@State(name="org.sanchouss.idea.plugins.instantpatch.InstantPatchRemotePlugin",
        storages = {@Storage(id = "default", file = "$APP_CONFIG$"+"/" +"InstantPatchRemotePluginRegistration.xml")
                /*@Storage(StoragePathMacros.APP_CONFIG)*/})
public class InstantPatchRemotePluginRegistration implements ApplicationComponent,
        PersistentStateComponent<PluginSettings> {
    public static String shortName = "Instant Patch Plugin";
    public static String name = "Instant Patch Remote Java Process Plugin";
    public static String notificationGroupId = name + " log";
    public static final String stateStorageLocation = StoragePathMacros.APP_CONFIG + File.separator;

    PluginSettings pluginSettings = new PluginSettings();

    // Returns the component name (any unique string value).
    @NotNull
    public String getComponentName() {
        return name;
    }

    // If you register the InstantPatchRemotePluginRegistration class in the <application-components> section of
    // the plugin.xml file, this method is called on IDEA start-up.
    public void initComponent() {
        ActionManager am = ActionManager.getInstance();
        Notifications.Bus.notify(new Notification(notificationGroupId, "Loading " + InstantPatchRemotePluginRegistration.shortName, "Loading started...", NotificationType.INFORMATION, NotificationListener.URL_OPENING_LISTENER));

        try {
            final RemoteOperationRootGroup rootAction = new RemoteOperationRootGroup(pluginSettings);

            // Passes an instance of your custom TextBoxes class to the registerAction method of the ActionManager class.
            am.registerAction(RemoteOperationRootGroup.class.getName(), rootAction);

            final AnAction ProjectViewPopupMenuAction = am.getAction("ProjectViewPopupMenu");
            if (!(ProjectViewPopupMenuAction instanceof DefaultActionGroup)) {
                System.err.print("ProjectViewPopupMenu is not instanceof DefaultActionGroup, but " + ProjectViewPopupMenuAction.getClass());
            }
            final DefaultActionGroup ProjectViewPopupMenuGroup = (DefaultActionGroup) ProjectViewPopupMenuAction;

            ProjectViewPopupMenuGroup.add(new Separator(), Constraints.FIRST);
            ProjectViewPopupMenuGroup.add(rootAction, Constraints.FIRST);

            Notifications.Bus.notify(new Notification(notificationGroupId, "Loading " + InstantPatchRemotePluginRegistration.shortName, "Loading finished...", NotificationType.INFORMATION, NotificationListener.URL_OPENING_LISTENER));
        } catch (Exception e) {
            System.err.println("Exception happened while registering root action group: " + e);
            Notifications.Bus.notify(new Notification(InstantPatchRemotePluginRegistration.notificationGroupId, "Loading " + InstantPatchRemotePluginRegistration.shortName,
                    "Loading error: " + e.toString(), NotificationType.ERROR, NotificationListener.URL_OPENING_LISTENER));
        }
    }

    // Disposes system resources.
    public void disposeComponent() {
    }

    @Nullable
    @Override
    public PluginSettings getState() {
        // save state to disk
        return this.pluginSettings;
    }

    @Override
    public void loadState(PluginSettings pluginState) {
        // load state from disk
        this.pluginSettings = pluginState;
    }

}