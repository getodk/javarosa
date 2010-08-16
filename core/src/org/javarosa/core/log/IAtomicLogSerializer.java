/**
 * 
 */
package org.javarosa.core.log;

/**
 * @author ctsims
 *
 */
public interface IAtomicLogSerializer {
	public boolean serializeLog(LogEntry entry);
}
