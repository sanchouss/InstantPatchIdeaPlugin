package org.sanchouss.idea.plugins.instantpatch;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

/**
 * not proven method to hook up, to be removed
 */
public class InstantPatchRemoteProjectListener
//        implements ProjectManagerListener
{
    private static final Logger logger = Logger.getInstance(InstantPatchRemoteProjectListener.class.getName());

//    @Override
    public void projectOpened(@NotNull Project project) {
        logger.info("InstantPatchRemoteProjectListener#projectOpened " + project.getName());
        System.out.println("InstantPatchRemoteProjectListener#projectOpened " + project.getName());
    }

//    @Override
    public boolean canCloseProject(Project project) {
        return true;
    }

//    @Override
    public void projectClosed(Project project) {

    }

//    @Override
    public void projectClosing(Project project) {

    }
}
