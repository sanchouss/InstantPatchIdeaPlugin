package org.sanchouss.idea.plugins.instantpatch;

import com.intellij.notification.Notification;
import com.intellij.notification.NotificationType;
import com.intellij.notification.Notifications;
import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.Service;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.openapi.diagnostic.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.sanchouss.idea.plugins.instantpatch.actions.RemoteOperationRootGroup;
import org.sanchouss.idea.plugins.instantpatch.settings.PluginSettingsState;
import org.sanchouss.idea.plugins.instantpatch.util.ExceptionUtils;

/* Application-Level Service
 ** Can hold state and keep it between two starts of IDE
 **
 */
@Service(Service.Level.APP)
// todo: remove platform-dependant path separator
@State(name = "org.sanchouss.idea.plugins.instantpatch.InstantPatchRemotePluginService",
        storages = @Storage("InstantPatchRemotePluginSettings.xml")
//        storages = {@Storage(value = "$APP_CONFIG$"+ SLASH_LINUX_STYLE +"InstantPatchRemotePluginService.xml")}
//                storages = @Storage(StoragePathMacros.APP_CONFIG)
)
public final class InstantPatchRemotePluginService implements PersistentStateComponent<PluginSettingsState> {
    private static final Logger logger = Logger.getInstance(InstantPatchRemotePluginService.class.getName());

    public static String shortName = "Instant Patch Plugin";
    public static String name = "Instant Patch Remote Java Process Plugin";
    public static String notificationGroupId = name + " log";

    private PluginSettingsState pluginSettingsState = new PluginSettingsState();
    private RemoteOperationRootGroup rootAction;

    public void initActions() {
        // todo: revise error reporting: stderr/logs/notifications
        try {
            // a hack to avoid duplication of menu items in case of project closing and reopening
            if (rootAction != null) {
                System.out.println("Project was loaded once already");
                return;
            }

            ActionManager am = ActionManager.getInstance();
            rootAction = new RemoteOperationRootGroup(pluginSettingsState);

            // Passes an instance of your custom TextBoxes class to the registerAction method of the ActionManager class.
            am.registerAction(RemoteOperationRootGroup.class.getName(), rootAction);

            final AnAction ProjectViewPopupMenuAction = am.getAction("ProjectViewPopupMenu");
            if (!(ProjectViewPopupMenuAction instanceof DefaultActionGroup)) {
                System.err.print("ProjectViewPopupMenu is not instanceof DefaultActionGroup, but " + ProjectViewPopupMenuAction.getClass());
            }
            final DefaultActionGroup ProjectViewPopupMenuGroup = (DefaultActionGroup) ProjectViewPopupMenuAction;

            ProjectViewPopupMenuGroup.add(new Separator(), Constraints.FIRST);
            ProjectViewPopupMenuGroup.add(rootAction, Constraints.FIRST);

            // for debugging; last arg deprecated (NotificationListener.URL_OPENING_LISTENER)
            Notifications.Bus.notify(new Notification(notificationGroupId, "Loading " + InstantPatchRemotePluginService.shortName, "Loading finished.", NotificationType.INFORMATION));
        } catch (Exception e) {
            System.err.println("Exception happened while registering root action group: " + e);
            Notifications.Bus.notify(new Notification(notificationGroupId, "Loading " + InstantPatchRemotePluginService.shortName,
                    "Loading error: " + ExceptionUtils.getStructuredErrorString(e), NotificationType.ERROR));
        }
    }


    @Nullable
    @Override
    public PluginSettingsState getState() {
        // save state to disk
        return this.pluginSettingsState;
    }

    @Override
    public void loadState(@NotNull PluginSettingsState pluginState) {
        // load state from disk
        this.pluginSettingsState = pluginState;
    }

    public static InstantPatchRemotePluginService getInstance() {
        InstantPatchRemotePluginService service =  ApplicationManager.getApplication().getService(InstantPatchRemotePluginService.class);
//        InstantPatchRemotePluginService service = ServiceManager.getService(InstantPatchRemotePluginService.class);
        if (service == null) {
            logger.error("plugin init failed: InstantPatchRemotePluginService is null");
            throw new RuntimeException("plugin init failed: InstantPatchRemotePluginService is null");
        }
        return service;
    }
}
//