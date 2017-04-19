package org.javarosa.core.reference;

/**
 * A ReferenceFactory is responsible for knowing how to derive a 
 * reference for a range of URI's. ReferenceFactories may or may 
 * not be present in different environments.
 * 
 * ReferenceFactory are not required to generate particular references, and
 * may rely on (or attempt to rely on) other factories in implementation,
 * negotiated through the reference manager.
 * 
 * In general, simple reference derivations should happen using a
 * PrefixedRootFactory, which handles most of the URI munging for you
 *  
 * @author ctsims
 *
 */
public interface ReferenceFactory {
	public boolean derives(String URI);
	public Reference derive(String URI) throws InvalidReferenceException;
	public Reference derive(String URI, String context) throws InvalidReferenceException;
}
