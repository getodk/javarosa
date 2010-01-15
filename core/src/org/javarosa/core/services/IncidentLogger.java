package org.javarosa.core.services;

import java.util.Date;

import org.javarosa.core.api.IIncidentLogger;
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
		if(JavaRosaPropertyRules.LOGS_ENABLED_YES.equals(PropertyManager._().getSingularProperty(JavaRosaPropertyRules.LOGS_ENABLED))){
			if(logger != null) {
				logger.logIncident(type, message, new Date());
			} else {
				System.out.println(type + ": " + message);
			}
		}
	}
}
