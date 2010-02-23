package org.javarosa.core.services;

import java.util.Date;

import org.javarosa.core.api.IIncidentLogger;
import org.javarosa.core.log.FatalException;
import org.javarosa.core.log.WrappedException;
import org.javarosa.core.services.properties.JavaRosaPropertyRules;

public class IncidentLogger {
    private static IIncidentLogger logger;

	public static void registerIncidentLogger(IIncidentLogger theLogger) {
		logger = theLogger;
	}
	
	public static IIncidentLogger _ () {
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
	public static void logIncident(String type, String message) {
		if (isLoggingEnabled()) {
			if(logger != null) {
				try {
					logger.logIncident(type, message, new Date());
				} catch (Exception e) {
					//TODO: do something better here
					System.err.println("exception when trying to write log message!");
				}
			} else {
				System.out.println(type + ": " + message);
			}
		}
	}
	
	public static boolean isLoggingEnabled () {
		boolean enabled;
		boolean problemReadingFlag = false;
		try {
			String flag = PropertyManager._().getSingularProperty(JavaRosaPropertyRules.LOGS_ENABLED);
			enabled = (flag == null || flag.equals(JavaRosaPropertyRules.LOGS_ENABLED));
		} catch (Exception e) {
			enabled = true;	//default to true if problem
			problemReadingFlag = true;
		}
		
		//TODO: do something better here
		if (problemReadingFlag) {
			System.err.println("error reading 'logging enabled' flag");
		}
		
		return enabled;
	}
	
	public static void logException (Exception e) {
		logException(e, false);
	}
	
	public static void logException (Exception e, boolean topLevel) {
		logIncident("exception", (topLevel ? "unhandled exception at top level: " : "") + WrappedException.printException(e));
	}
	
	public static void die (String thread, Exception e) {
		//log exception
		logException(e, true);
				
		//crash
		throw new FatalException("unhandled exception in " + thread, e);
	}
	
	public static void crashTest (String msg) {
		throw new FatalException(msg != null ? msg : "shit has hit the fan");
	}
}

