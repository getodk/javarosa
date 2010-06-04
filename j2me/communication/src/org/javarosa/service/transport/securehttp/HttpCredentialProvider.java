/**
 * 
 */
package org.javarosa.service.transport.securehttp;

/**
 * An HttpCredentialProvider is capable of acquiring and
 * returning credentials for HTTP authentication. 
 * 
 * Implementing classes should exist three stages:
 * <ul>
 * <li>Uninitialized - After construction: Unable to provided credentials</li>
 * <li>Acquired - After acquireCredentials: Able to provide credentials</li>
 * <li>Failed - After acquireCredentials: Unable to provide credentials</li>
 * </ul>
 * 
 * In either the Uninitialized or Failed state, acquireCredentials should 
 * move the state to Acquired on a positive return value, or Failed on
 * a negative one.
 * 
 * acquireCredentials can block on response for user input, but it is not guaranteed
 * to be polled efficiently in such a way that it ensures that it is not
 * called in the UI thread yet.
 * 
 * @author ctsims
 *
 */
public interface HttpCredentialProvider {
	
	/**
	 * Possibly blocking call to prepare credentials for authentication.
	 * 
	 * @return True if credentials were acquired and ready for use, False
	 * if none could be provided.
	 */
	public boolean acquireCredentials();
	
	/**
	 * Only valid in the Acquired State
	 * 
	 * @return The username for authentication
	 */
	public String getUsername();
	
	/**
	 * Only valid in the Acquired State
	 * 
	 * @return The password for authentication
	 */
	public String getPassword();
	
}
