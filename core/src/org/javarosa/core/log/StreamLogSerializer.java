/**
 *
 */
package org.javarosa.core.log;

import java.io.IOException;

import org.javarosa.core.util.SortedIntSet;

/**
 * @author ctsims
 *
 */
public abstract class StreamLogSerializer {

	SortedIntSet logIDs;
	Purger purger = null;

	public interface Purger {
		void purge(SortedIntSet IDs);
	}

	public StreamLogSerializer () {
		logIDs = new SortedIntSet();
	}

	public final void serializeLog(int id, LogEntry entry) throws IOException {
		logIDs.add(id);
		serializeLog(entry);
	}

	protected abstract void serializeLog(LogEntry entry) throws IOException;

	public void setPurger (Purger purger) {
		this.purger = purger;
	}

	public void purge() {
		//The purger is optional, not mandatory.
		if(purger != null) {
			this.purger.purge(logIDs);
		}
	}
}
