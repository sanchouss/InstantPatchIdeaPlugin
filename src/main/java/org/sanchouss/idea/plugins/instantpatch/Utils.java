package org.sanchouss.idea.plugins.instantpatch;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by Alexander Perepelkin
 */
public class Utils {
    private static final ExecutorService executorService = Executors.newSingleThreadExecutor();

    public static ExecutorService getExecutorService() {
        return executorService;
    }

}
