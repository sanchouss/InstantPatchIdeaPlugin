package org.sanchouss.idea.plugins.instantpatch;

import com.intellij.notification.Notification;
import com.intellij.notification.NotificationListener;
import com.intellij.notification.NotificationType;
import com.intellij.notification.Notifications;
import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import org.jetbrains.annotations.Nullable;
import org.sanchouss.idea.plugins.instantpatch.actions.RemoteOperationRootGroup;
import org.sanchouss.idea.plugins.instantpatch.settings.PluginSettings;
import org.sanchouss.idea.plugins.instantpatch.util.ExceptionUtils;

import static org.sanchouss.idea.plugins.instantpatch.Checks.SLASH_LINUX_STYLE;

// todo: remove platform-dependant path separator
@State(name="org.sanchouss.idea.plugins.instantpatch.InstantPatchRemotePlugin",
        storages = {@Storage(value = "$APP_CONFIG$"+ SLASH_LINUX_STYLE +"InstantPatchRemotePluginRegistration.xml")
                /*@Storage(StoragePathMacros.APP_CONFIG)*/})
public class InstantPatchRemotePluginService implements PersistentStateComponent<PluginSettings> {
    public static String shortName = "Instant Patch Plugin";
    public static String name = "Instant Patch Remote Java Process Plugin";
    public static String notificationGroupId = name + " log";

    PluginSettings pluginSettings = new PluginSettings();

    public void initActions() {
        PluginSettings pluginSettings = new PluginSettings();
        try {
            ActionManager am = ActionManager.getInstance();
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

            // for debugging
            Notifications.Bus.notify(new Notification(notificationGroupId, "Loading " + InstantPatchRemotePluginRegistration.shortName, "Loading finished...", NotificationType.INFORMATION, NotificationListener.URL_OPENING_LISTENER));
        } catch (Exception e) {
            System.err.println("Exception happened while registering root action group: " + e);
            Notifications.Bus.notify(new Notification(InstantPatchRemotePluginRegistration.notificationGroupId, "Loading " + InstantPatchRemotePluginRegistration.shortName,
                    "Loading error: " + ExceptionUtils.getStructuredErrorString(e), NotificationType.ERROR, NotificationListener.URL_OPENING_LISTENER));
        }
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
