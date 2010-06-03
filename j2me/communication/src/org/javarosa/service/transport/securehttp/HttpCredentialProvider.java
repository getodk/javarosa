/**
 * 
 */
package org.javarosa.service.transport.securehttp;

/**
 * @author ctsims
 *
 */
public interface HttpCredentialProvider {
	
	public boolean acquireCredentials();
	
	public String getUsername();
	
	public String getPassword();
	
}
