package org.sanchouss.idea.plugins.instantpatch;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.startup.ProjectActivity;
import kotlin.Unit;
import kotlin.coroutines.Continuation;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/* // earlier found
public class InstantPatchRemoteStartupActivity implements StartupActivity.DumbAware {
    @Override
    public void runActivity(Project project) {
        // Your startup code here
        System.out.println("MyStartupActivity is running for project: " + project.getName());
    }
}*/

/**
 * Runs after project is loaded and generates menu actions.
 * Comes with IDEA ver. 2023.1
 */
public class InstantPatchRemoteStartupActivity implements ProjectActivity {
    /**
     * This routes messages through IntelliJ's logging system, which you can view in:
     * Help > Show Log in Explorer/Finder
     * or idea.log in the sandbox dir (/logs/idea.log, <project-root>/build/idea-sandbox/system/log/idea.log)
     */
    private static final Logger logger = Logger.getInstance(InstantPatchRemoteStartupActivity.class.getName());

    @Nullable
    @Override
    public Object execute(@NotNull Project project, @NotNull Continuation<? super Unit> continuation) {
        logger.info("InstantPatchRemoteStartupActivity#execute " + project.getName());
        System.out.println("InstantPatchRemoteStartupActivity#execute " + project.getName());

        InstantPatchRemotePluginService service = InstantPatchRemotePluginService.getInstance();
        service.initActions();

        return null;
    }
}