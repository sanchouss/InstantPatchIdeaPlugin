package org.sanchouss.idea.plugins.instantpatch;

import com.intellij.notification.Notification;
import com.intellij.notification.NotificationType;
import com.intellij.notification.Notifications;
import com.intellij.openapi.application.Application;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static org.sanchouss.idea.plugins.instantpatch.InstantPatchRemotePluginService.notificationGroupId;

/**
 * Obsolete method to hook up, to be removed
 */
public class InstantPatchRemoteAppLifecycleListener
//        implements com.intellij.ide.AppLifecycleListener, com.intellij.openapi.wm.ex.ToolWindowManagerListener
{
    public void appStarting(@Nullable Project projectFromCommandLine) {
        Application application = ApplicationManager.getApplication();
        Notifications.Bus.notify(new Notification(notificationGroupId, "appStarting event " + InstantPatchRemotePluginService.shortName,
                "appStarting..., ApplicationManager.getApplication()=" + application + " thread=" + Thread.currentThread().getName(),
                NotificationType.INFORMATION));
    }

//    @Override
    public void appStarted() {
        Application application = ApplicationManager.getApplication();
        Notifications.Bus.notify(new Notification(notificationGroupId, "appStarted event " + InstantPatchRemotePluginService.shortName,
                "appStarted..., ApplicationManager.getApplication()=" + application + " thread=" + Thread.currentThread().getName(),
                NotificationType.INFORMATION));

    }

//    @Override
//    @Deprecated
    public void toolWindowRegistered(@NotNull String id) {
        Application application = ApplicationManager.getApplication();
        Notifications.Bus.notify(new Notification(notificationGroupId, "toolWindowRegistered event " + id,
                "toolWindowRegistered " + id + " ApplicationManager.getApplication()=" + application
                        + " thread=" + Thread.currentThread().getName(),
                NotificationType.INFORMATION));

        if (id.equals("Project")) {
            InstantPatchRemotePluginService.getInstance().initActions();
        }
    }
}
