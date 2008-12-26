package org.javarosa.core.services.transport;

import java.io.InputStream;

import org.javarosa.core.util.externalizable.Externalizable;

/**
 * IDataPayload is an interface that specifies a piece of data
 * that will be transmitted over the wire to  
 *  
 * @author Clayton Sims
 * @date Dec 18, 2008 
 *
 */
public interface IDataPayload extends Externalizable {
	/**
	 * Gets the stream for this payload.
	 * 
	 * @return A stream for the data in this payload.
	 */
	public InputStream getPayloadStream();
	
	/**
	 * @return The Id of this payload
	 */
	public String getPayloadId();
	
	/**
	 * @return The type of the data encapsulated by this
	 * payload.
	 */
	public int getPayloadType();
	
	/**
	 * Visitor pattern accept method.
	 * @param visitor The visitor to visit this payload.
	 */
	public Object accept(IDataPayloadVisitor visitor);
	
	public int getLength();
}
