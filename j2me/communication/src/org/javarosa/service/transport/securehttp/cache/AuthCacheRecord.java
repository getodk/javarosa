package org.javarosa.service.transport.securehttp.cache;

import org.javarosa.service.transport.securehttp.AuthenticatedHttpTransportMessage;

public interface AuthCacheRecord {
	
	public String getUrl();
	
	public boolean invalidates(AuthCacheRecord record);
	
	public boolean validFor(String URI);
	
	public String retrieve(AuthenticatedHttpTransportMessage message);
}
