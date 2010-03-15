package org.javarosa.core.reference;

/**
 * A Root is responsible for knowing how to derive a reference 
 * for a range of URI's. Roots may or may not be present in 
 * different environments.
 * 
 * Roots are not required to generate particular references, and
 * may rely on (or attempt to rely on) other roots in implementation,
 * negotiated through the reference manager.
 *  
 * @author ctsims
 *
 */
public interface RawRoot {
	public boolean derives(String URI);
	public Reference derive(String URI) throws InvalidReferenceException;
	public Reference derive(String URI, String context) throws InvalidReferenceException;
}
