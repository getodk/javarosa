/**
 * 
 */
package org.javarosa.service.transport.securehttp;

/**
 * @author ctsims
 *
 */
public class DefaultHttpCredentialProvider implements HttpCredentialProvider {

	String username;
	String password;
	
	public DefaultHttpCredentialProvider(String username, String password) {
		this.username = username;
		this.password = password;
	}
	
	/* (non-Javadoc)
	 * @see org.javarosa.service.transport.securehttp.HttpCredentialProvider#acquireCredentials()
	 */
	public boolean acquireCredentials() {
		if(this.username != null && this.password != null) {
			return true;
		} else {
			return false;
		}
	}

	/* (non-Javadoc)
	 * @see org.javarosa.service.transport.securehttp.HttpCredentialProvider#getPassword()
	 */
	public String getPassword() {
		return password;
	}

	/* (non-Javadoc)
	 * @see org.javarosa.service.transport.securehttp.HttpCredentialProvider#getUsername()
	 */
	public String getUsername() {
		return username;
	}

}
