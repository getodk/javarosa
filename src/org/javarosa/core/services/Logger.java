package org.javarosa.core.services;

import org.javarosa.core.log.FatalException;
import org.javarosa.core.services.properties.JavaRosaPropertyRules;
import org.slf4j.LoggerFactory;

/**
 * <b>Warning:</b> This class is unused and should remain that way. It will be removed in a future release.
 *
 * This class depends on ILogger, which is also deprecated. We need to ignore any
 * deprecation warnings in order to avoid making breaking changes to this class
 * before removing it on a next release
 *
 * @deprecated Use {@link org.slf4j.LoggerFactory#getLogger(Class)} instead
 */
@Deprecated
@SuppressWarnings("deprecation")
public class Logger {
    private static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(Logger.class);
    /**
     * @deprecated Use {@link org.slf4j.LoggerFactory#getLogger(Class)} instead
     */
    @Deprecated
    public static final int MAX_MSG_LENGTH = 2048;


    /**
     * @deprecated Use {@link org.slf4j.LoggerFactory#getLogger(Class)} instead
     */
    @Deprecated
    public static void registerLogger(org.javarosa.core.api.ILogger theLogger) {
        LOGGER.warn("Using deprecated ILogger class. All logs will be redirected to SLF4J. Please migrate your code to SLF4J");
    }

    /**
     * @deprecated Use {@link org.slf4j.LoggerFactory#getLogger(Class)} instead
     */
    @Deprecated
    public static org.javarosa.core.api.ILogger __() {
        return null;
    }

    /**
     * Posts the given data to an existing Incident Log, if one has
     * been registered and if logging is enabled on the device.
     * <p>
     * NOTE: This method makes a best faith attempt to log the given
     * data, but will not produce any output if such attempts fail.
     *
     * @param type    The type of incident to be logged.
     * @param message A message describing the incident.
     * @deprecated Use {@link org.slf4j.Logger#info(String)} instead
     */
    @Deprecated
    public static void log(String type, String message) {
        if (isLoggingEnabled()) {
            logForce(type, message);
        }
    }

    @Deprecated
    protected static void logForce(String type, String message) {
        LOGGER.error("{}: {}", type, message);
    }

    @Deprecated
    public static boolean isLoggingEnabled() {
        boolean enabled;
        boolean problemReadingFlag = false;
        try {
            String flag = PropertyManager._().getSingularProperty(JavaRosaPropertyRules.LOGS_ENABLED);
            enabled = (flag == null || flag.equals(JavaRosaPropertyRules.LOGS_ENABLED_YES));
        } catch (Exception e) {
            enabled = true;    //default to true if problem
            problemReadingFlag = true;
        }

        if (problemReadingFlag) {
            logForce("log-error", "could not read 'logging enabled' flag");
        }

        return enabled;
    }

    /**
     * @deprecated Use {@link org.slf4j.Logger#error(String, Throwable)} instead
     */
    @Deprecated
    public static void exception(Exception e) {
        exception(null, e);
    }

    /**
     * @deprecated Use {@link org.slf4j.Logger#error(String, Throwable)} instead
     */
    @Deprecated
    public static void exception(String info, Exception e) {
        LOGGER.error(info, e);
    }

    @Deprecated
    public static void die(String thread, Exception e) {
        LOGGER.error("unhandled exception at top level", e);

        //crash
        final FatalException crashException = new FatalException("unhandled exception in " + thread, e);

        //depending on how the code was invoked, a straight 'throw' won't always reliably crash the app
        //throwing in a thread should work (at least on our nokias)
        new Thread() {
            public void run() {
                throw crashException;
            }
        }.start();

        //still do plain throw as a fallback
        try {
            Thread.sleep(3000);
        } catch (InterruptedException ie) {
        }
        throw crashException;
    }

    @Deprecated
    public static void crashTest(String msg) {
        throw new FatalException(msg != null ? msg : "shit has hit the fan");
    }

    @Deprecated
    public static void halt() {
    }
}

