package org.javarosa.core.services;

import java.util.Date;

import org.javarosa.core.api.ILogger;
import org.javarosa.core.log.FatalException;
import org.javarosa.core.log.WrappedException;
import org.javarosa.core.services.properties.JavaRosaPropertyRules;

public class Logger {
	public static final int MAX_MSG_LENGTH = 2048;
	
    private static ILogger logger;

	public static void registerLogger(ILogger theLogger) {
		logger = theLogger;
	}
	
	public static ILogger _ () {
		return logger;
	}
	
	/**
	 * Posts the given data to an existing Incident Log, if one has
	 * been registered and if logging is enabled on the device. 
	 * 
	 * NOTE: This method makes a best faith attempt to log the given
	 * data, but will not produce any output if such attempts fail.
	 * 
	 * @param type The type of incident to be logged. 
	 * @param message A message describing the incident.
	 */
	public static void log(String type, String message) {
		if (isLoggingEnabled()) {
			logForce(type, message);
		}
	}
	
	protected static void logForce(String type, String message) {
		System.err.println("logger> " + type + ": " + message);
		if (message.length() > MAX_MSG_LENGTH)
			System.err.println("  (message truncated)");
		
		message = message.substring(0, Math.min(message.length(), MAX_MSG_LENGTH));
		if(logger != null) {
			try {
				logger.log(type, message, new Date());
			} catch (RuntimeException e) {
				//do not catch exceptions here; if this fails, we want the exception to propogate
				System.err.println("exception when trying to write log message! " + WrappedException.printException(e));
				logger.panic();
				
				//be conservative for now
				//throw e;
			}
		}
	}
	
	public static boolean isLoggingEnabled () {
		boolean enabled;
		boolean problemReadingFlag = false;
		try {
			String flag = PropertyManager._().getSingularProperty(JavaRosaPropertyRules.LOGS_ENABLED);
			enabled = (flag == null || flag.equals(JavaRosaPropertyRules.LOGS_ENABLED_YES));
		} catch (Exception e) {
			enabled = true;	//default to true if problem
			problemReadingFlag = true;
		}
		
		if (problemReadingFlag) {
			logForce("log-error", "could not read 'logging enabled' flag");
		}
		
		return enabled;
	}
	
	public static void exception (Exception e) {
		exception(null, e);
	}
	
	public static void exception (String info, Exception e) {
		e.printStackTrace();
		log("exception", (info != null ? info + ": " : "") + WrappedException.printException(e));
	}
	
	public static void die (String thread, Exception e) {
		//log exception
		exception("unhandled exception at top level", e);
				
		//print stacktrace
		e.printStackTrace();
		
		//crash
		final FatalException crashException = new FatalException("unhandled exception in " + thread, e);
		
		//depending on how the code was invoked, a straight 'throw' won't always reliably crash the app
		//throwing in a thread should work (at least on our nokias)
		new Thread() {
			public void run () {
				throw crashException;
			}
		}.start();
		
		//still do plain throw as a fallback
		try {
			Thread.sleep(3000);
		} catch (InterruptedException ie) { }
		throw crashException;
	}
	
	public static void crashTest (String msg) {
		throw new FatalException(msg != null ? msg : "shit has hit the fan");
	}
	
	public static void halt() {
		if(logger != null) {
			logger.halt();
		}
	}
}

