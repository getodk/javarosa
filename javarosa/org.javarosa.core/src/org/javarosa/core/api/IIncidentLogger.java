/**
 * 
 */
package org.javarosa.core.api;

import java.util.Date;

import org.javarosa.core.log.ILogSerializer;

/**
 * IIncidentLogger's are used for instrumenting applications to identify usage
 * patterns, usability errors, and general trajectories through applications.
 * 
 * @author Clayton Sims
 * @date Apr 10, 2009 
 *
 */
public interface IIncidentLogger {
	
	public void logIncident(String type, String message, Date logDate);
	
	public void clearLogs();
	
	public byte[] serializeLogs(ILogSerializer serializer);
}
