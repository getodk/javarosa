/**
 * 
 */
package org.javarosa.core.log;

import java.io.IOException;

/**
 * @author ctsims
 *
 */
public interface IAtomicLogSerializer {
	public void serializeLog(LogEntry entry) throws IOException;
}
