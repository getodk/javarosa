/**
 * 
 */
package org.javarosa.core.log;

/**
 * @author Clayton Sims
 * @date Apr 10, 2009 
 *
 */
public class FlatLogSerializer implements ILogSerializer {

	/* (non-Javadoc)
	 * @see org.javarosa.core.log.ILogSerializer#serializeLog(org.javarosa.core.log.IncidentLog)
	 */
	private String serializeLog(IncidentLog log) {
		return "[" + log.getType() + "] " +log.getTime().toString() + ": " +  log.message+ "\n"; 
	}

	/* (non-Javadoc)
	 * @see org.javarosa.core.log.ILogSerializer#serializeLogs(org.javarosa.core.log.IncidentLog[])
	 */
	public byte[] serializeLogs(IncidentLog[] logs) {
		String log = ""; 
		for(int i = 0; i < logs.length; ++i ) {
			log += this.serializeLog(logs[i]);
		}
		return log.getBytes();
	}

}
