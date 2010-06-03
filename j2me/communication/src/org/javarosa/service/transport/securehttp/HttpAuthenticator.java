package org.javarosa.service.transport.securehttp;

import java.util.Hashtable;
import java.util.Vector;

import javax.microedition.io.HttpConnection;

import org.javarosa.core.model.utils.DateUtils;
import org.javarosa.service.transport.securehttp.cache.AuthorizationCache;
import org.javarosa.service.transport.securehttp.cache.DigestAuthResponse;

public class HttpAuthenticator {
	
	boolean attemptCache = false;
	
	HttpCredentialProvider provider;
	
	public HttpAuthenticator(HttpCredentialProvider provider) {
		this(provider,false);
	}
	
	public HttpAuthenticator(HttpCredentialProvider provider, boolean attemptCache) {
		this.provider = provider;
		this.attemptCache = attemptCache;
	}
	
	public String challenge(HttpConnection connection, String challenge, AuthenticatedHttpTransportMessage message) {
		String type = challenge.substring(0, challenge.indexOf(' '));
		String args = challenge.substring(challenge.indexOf(' ') + 1);
		if(type.equals("Digest")) {
			return digestResponse(connection,args, message);
		} else {
			return null;
		}
	}
	
	public String checkCache(AuthenticatedHttpTransportMessage message) {
		if(attemptCache) {
			return AuthorizationCache.load().retrieveAuthHeader(message);
		} else {
			return null;
		}
	}
	
	protected final String digestResponse(HttpConnection connection, String args, AuthenticatedHttpTransportMessage message) {
		Hashtable<String, String> params = AuthUtils.getQuotedParameters(args);
		
		if(provider == null || !provider.acquireCredentials()) {
			return null;
		}
		
		
		String username = provider.getUsername();
		String HA1 = AuthUtils.MD5(username + ":" + params.get("realm") + ":" + provider.getPassword());
		
		DigestAuthResponse response = new DigestAuthResponse(connection.getURL(), HA1);
		
		
		String qop;
		
		if(!params.containsKey("qop")) {
			qop = DigestAuthResponse.QOP_UNSPECIFIED;
		} else {
			Vector<String> qops = DateUtils.split(params.get("qop"),",",false);
			if(qops.contains(DigestAuthResponse.QOP_AUTH_INT) && qops.contains(DigestAuthResponse.QOP_AUTH)) {
				//choose between auth-int and auth if both are available;
				qop = DigestAuthResponse.QOP_AUTH;
			} else if(qops.size() == 1) {
				if(qops.elementAt(0).equals(DigestAuthResponse.QOP_AUTH)) {
					qop = DigestAuthResponse.QOP_AUTH;
				} else if(qops.elementAt(0).equals(DigestAuthResponse.QOP_AUTH_INT)) {
					qop = DigestAuthResponse.QOP_AUTH_INT;
				} else {
					return null;
				}
			} else {
				//These are really the only possibilities...
				return null;
			}
		}
		
		String uri;
		if(params.containsKey("domain")) {
			uri = params.get("domain");
		} else {
			//TODO: This should get cut off at the end
			uri = connection.getURL();
		}
		
		String nonce = params.get("nonce");
		
		String opaque = params.get("opaque");
		
		response.put("username", AuthUtils.quote(username));
		response.put("realm", AuthUtils.quote(params.get("realm")));
		response.put("nonce", AuthUtils.quote(nonce));
		response.put("uri", AuthUtils.quote(uri));
		
		if(qop != DigestAuthResponse.QOP_UNSPECIFIED) {
			response.put("qop", qop);
		}
		
		if(opaque != null) {
			response.put("opaque",AuthUtils.quote(opaque));	
		}
		
		String header = response.buildResponse(message);
		AuthorizationCache.load().cache(response);
		return header;
	}
}
