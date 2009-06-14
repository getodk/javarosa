package org.javarosa.core.services.transport;

import org.javarosa.core.util.externalizable.Externalizable;

/**
 * A Transport Destination contains all of the information required to
 * deliver a message to another device. In the case of a URL, this
 * might be as simple as a String, but may contain more structured
 * information in the case of an SMS (Number/Port) or bluetooth 
 * partnership.
 * 
 * 
 * @author Clayton Sims
 *
 */
public interface ITransportDestination extends Externalizable {

}
