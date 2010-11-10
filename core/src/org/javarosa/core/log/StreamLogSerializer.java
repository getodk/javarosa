/**
 * 
 */
package org.javarosa.core.log;

import java.io.IOException;

import org.javarosa.core.services.Logger;
import org.javarosa.core.util.SortedIntSet;

/**
 * @author ctsims
 *
 */
public abstract class StreamLogSerializer {
	
	SortedIntSet logIDs;
	
	public StreamLogSerializer () {
		logIDs = new SortedIntSet();
	}
	
	public final void serializeLog(int id, LogEntry entry) throws IOException {
		logIDs.add(id);
		serializeLog(entry);
	}
	
	protected abstract void serializeLog(LogEntry entry) throws IOException;
	
	public void purge() {
		Logger._().clearLogs(logIDs);
	}
}
