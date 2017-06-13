package org.sanchouss.idea.plugins.instantpatch;

/**
 * Created by Alexander Perepelkin
 */
public class Checks {

    public static final String SLASH_LINUX_STYLE = "/";

    public static void checkEndsWithSlash(String directory) {
        if (!directory.endsWith(SLASH_LINUX_STYLE)) {
            throw new RuntimeException("Process directory should end with / !");
        }
    }
}
