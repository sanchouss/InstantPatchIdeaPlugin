package org.sanchouss.idea.plugins.instantpatch;

import com.intellij.notification.Notification;
import com.intellij.notification.NotificationListener;
import com.intellij.notification.NotificationType;
import com.intellij.notification.Notifications;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static org.sanchouss.idea.plugins.instantpatch.InstantPatchRemotePluginService.notificationGroupId;

public class InstantPatchRemoteAppLifecycleListener implements com.intellij.ide.AppLifecycleListener,
        com.intellij.openapi.wm.ex.ToolWindowManagerListener {
    public void appStarting(@Nullable Project projectFromCommandLine) {
        ApplicationManager.getApplication();
        Notifications.Bus.notify(new Notification(notificationGroupId, "appStarting " + InstantPatchRemotePluginRegistration.shortName,
                "appStarting..., ApplicationManager.getApplication()=" + ApplicationManager.getApplication() + " thread=" + Thread.currentThread().getName(),
                NotificationType.INFORMATION, NotificationListener.URL_OPENING_LISTENER));
    }

    @Override
    public void toolWindowRegistered(@NotNull String id) {
        Notifications.Bus.notify(new Notification(notificationGroupId, "toolWindowRegistered " + id,
                "toolWindowRegistered " + id + " ApplicationManager.getApplication()=" + ApplicationManager.getApplication() + " thread=" + Thread.currentThread().getName(),
                NotificationType.INFORMATION, NotificationListener.URL_OPENING_LISTENER));
        if (id.equals("Project")) {
            ApplicationManager.getApplication();
            InstantPatchRemotePluginService service = ServiceManager.getService(InstantPatchRemotePluginService.class);
//            service.initActions();
        }
    }
}
