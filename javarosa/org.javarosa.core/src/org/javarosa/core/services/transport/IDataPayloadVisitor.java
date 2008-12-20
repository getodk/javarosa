/**
 * 
 */
package org.javarosa.core.services.transport;

/**
 * @author Clayton Sims
 * @date Dec 18, 2008 
 *
 */
public interface IDataPayloadVisitor {
	public Object visit(ByteArrayPayload payload);
	public Object visit(MultiMessagePayload payload);
}
