package org.sanchouss.idea.plugins.instantpatch;

/**
 * Created by Alexander Perepelkin
 */
public class Checks {
    public static void checkEndsWithSlash(String directory) {
        if (!directory.endsWith("/")) {
            throw new RuntimeException("Process directory should end with / !");
        }
    }
}
