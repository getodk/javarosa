package org.javarosa.service.transport.securehttp.cache;

import org.javarosa.service.transport.securehttp.AuthenticatedHttpTransportMessage;

/**
 * An AuthCacheRecord is simply a record of a set of authorization
 * headers which can be used to authenticate future mesages.  
 * 
 * @author ctsims
 *
 */
public interface AuthCacheRecord {
	
	/**
	 * @return The URL that this record authenticates.
	 */
	public String getUrl();
	
	/**
	 * Determines whether the provided record is invalidated
	 * by this record. 
	 * 
	 * This is often a simple URL to URL match, but can be 
	 * more broad. For instance, digest authentication records
	 * should invalidate a full domain.
	 * 
	 * @param record The record which currently exists.
	 * 
	 * @return True if this record supercedes the validity of
	 * the provided record. False otherwise.
	 */
	public boolean invalidates(AuthCacheRecord record);
	
	/**
	 * Determines whether this record can attempt to authenticate
	 * a request to the provided URL
	 * @param URL
	 * @return True if this record can return a potential authentication,
	 * false otherwise.
	 */
	public boolean validFor(String URL);
	
	/**
	 * Note: This is a stateful operation.
	 * 
	 * @param message
	 * @return An HTTP Authenticate header which can be used to potentially
	 * authenticate the provided message. Null if no such header can be
	 * provided.
	 */
	public String retrieve(AuthenticatedHttpTransportMessage message);
}
