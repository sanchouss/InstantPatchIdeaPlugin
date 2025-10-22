package org.sanchouss.idea.plugins.instantpatch.util;

import java.io.PrintWriter;
import java.io.StringWriter;

public class ExceptionUtils {
    /**
     * Limiting length of error message for displaying in notification window
     */
    public static String getStructuredErrorString2(Exception e) {
        final StringWriter sw = new StringWriter(1024);
        final PrintWriter pw = new PrintWriter(sw);
        e.printStackTrace(pw);
        return sw.toString();
    }

    /**
     * Fallback to standard message
     */
    public static String getStructuredErrorString(Exception e) {
        return e.getMessage();
    }

}
