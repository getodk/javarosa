/**
 * 
 */
package org.javarosa.core.services.transport;

import java.io.InputStream;

import org.javarosa.core.util.externalizable.Externalizable;

/**
 * @author Clayton Sims
 * @date Dec 18, 2008 
 *
 */
public interface ITransportHeader extends Externalizable {
	public int getTransportType();
	public InputStream getHeaderStream();
}
