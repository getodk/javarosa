/**
 * 
 */
package org.javarosa.service.transport.securehttp.cache;

import java.util.Vector;

import org.javarosa.service.transport.securehttp.AuthenticatedHttpTransportMessage;

/**
 * The authorization cache contains records of authorization
 * headers which can then be re-used for futher authorization
 * at a later time. 
 * 
 * @author ctsims
 *
 */
public class AuthorizationCache {
	
	private Vector<AuthCacheRecord> records;
	private static AuthorizationCache _;
	
	private AuthorizationCache() {
	}
	
	/**
	 * Loads the cache and makes it available for use.
	 * @return The Authorization Cache.
	 */
	public static AuthorizationCache load() {
		if(_ == null) {
			_ = new AuthorizationCache();
			_.records = new Vector<AuthCacheRecord>();
		}
		//TODO: We probably need a resource release handle
		//or something here...
		return _;
	}
	
	/**
	 * Caches the provided record for future use.
	 * @param record
	 */
	public void cache(AuthCacheRecord record) {
		Vector<AuthCacheRecord> invalidated = new Vector<AuthCacheRecord>();
		for(AuthCacheRecord old : records) {
			if(record.invalidates(old)) {
				invalidated.addElement(old);
			}
		}
		for(AuthCacheRecord invalid : invalidated) {
			this.records.removeElement(invalid);
		}
		records.addElement(record);
	}
	
	/** 
	 * @param message
	 * @return An authentication header for the provided message if one could
	 * be created. Null if none could.
	 */
	public String retrieveAuthHeader(AuthenticatedHttpTransportMessage message) {
		for(AuthCacheRecord r: records) {
			if(r.validFor(message.getUrl())) {
				return r.retrieve(message);
			}
		}
		return null;
	}
}
