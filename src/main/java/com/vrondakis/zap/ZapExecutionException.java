/* HEADER */
package com.vrondakis.zap;

import org.apache.commons.lang.exception.ExceptionUtils;

import java.io.PrintStream;

public class ZapExecutionException extends Exception {

    public ZapExecutionException(String message) {
        super(message);
    }

    public ZapExecutionException(String message, Throwable cause) {
        super(message, cause);
    }

    public ZapExecutionException(String message, PrintStream logger) {
        super(message);
        logMessage(message, logger);
    }

    public ZapExecutionException(String message, Throwable cause, PrintStream logger) {
        super(message, cause);
        logMessage(message, logger);
        logMessage("Caused by: " + cause.getMessage(), logger);
        logMessage(ExceptionUtils.getStackTrace(cause), logger);
    }

    private void logMessage(String message, PrintStream logger) {
        if (logger != null) {
            logger.println("zap: " + message);
        }
    }
}
