package org.sanchouss.idea.plugins.instantpatch;

import com.intellij.notification.Notification;
import com.intellij.notification.NotificationListener;
import com.intellij.notification.NotificationType;
import com.intellij.notification.Notifications;
import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.Constraints;
import com.intellij.openapi.actionSystem.DefaultActionGroup;
import com.intellij.openapi.actionSystem.Separator;
import com.intellij.openapi.components.*;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.sanchouss.idea.plugins.instantpatch.actions.RemoteOperationRootGroup;
import org.sanchouss.idea.plugins.instantpatch.settings.PluginSettings;
import org.sanchouss.idea.plugins.instantpatch.util.ExceptionUtils;

import java.io.File;

import static org.sanchouss.idea.plugins.instantpatch.Checks.SLASH_LINUX_STYLE;

/**
 * Created by Alexander Perepelkin
 *
 */
public class InstantPatchRemotePluginRegistration implements ApplicationComponent
        {
    public static String shortName = "Instant Patch Plugin";
    public static String name = "Instant Patch Remote Java Process Plugin";
    public static String notificationGroupId = name + " log";
//    public static final String stateStorageLocation = StoragePathMacros.APP_CONFIG + File.separator;

    PluginSettings pluginSettings = new PluginSettings();

    // Returns the component name (any unique string value).
    @NotNull
    public String getComponentName() {
        return name;
    }

    private void addActionGroupToMenu() {
        // for debugging
        Notifications.Bus.notify(new Notification(notificationGroupId, "Loading " + InstantPatchRemotePluginRegistration.shortName, "Loading started...", NotificationType.INFORMATION, NotificationListener.URL_OPENING_LISTENER));

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

    // If you register the InstantPatchRemotePluginRegistration class in the <application-components> section of
    // the plugin.xml file, this method is called on IDEA start-up.
    public void initComponent() {
        addActionGroupToMenu();
    }

    // Disposes system resources.
    public void disposeComponent() {
    }
}