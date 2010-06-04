package org.javarosa.service.transport.securehttp;

import java.util.Hashtable;
import java.util.Vector;

import javax.microedition.io.HttpConnection;

import org.javarosa.core.model.utils.DateUtils;
import org.javarosa.service.transport.securehttp.cache.AuthorizationCache;
import org.javarosa.service.transport.securehttp.cache.DigestAuthResponse;

/**
 * An Http Authenticator is responsible for receiving WWW-Authenticate
 * challenges, and determining the appropriate Authenticate response. 
 * 
 * If directed, it will also attempt to retrieve Authentications ahead
 * of time for specific URL's from a cache, allowing commonly accessed
 * servers to not require the overhead of the credential negotiation.
 * 
 * It requires a credential provider to be handed to it to provide
 * credentials upon request. For now, it is assumed that the authenticator
 * itself is running in a thread that is not the UI thread, so that the
 * CredentialProvider can block on acquiring credentials and allow for
 * user interaction if necessary.
 * 
 * @author ctsims
 *
 */
public class HttpAuthenticator {
	
	boolean attemptCache = false;
	
	HttpCredentialProvider provider;
	
	/**
	 * Creates an Authenticator which retrieves it's credentials (if necessary)
	 * from provider
	 * 
	 * @param provider A provider capable of retrieving HTTP credentials upon
	 * request.
	 */
	public HttpAuthenticator(HttpCredentialProvider provider) {
		this(provider,false);
	}
	
	/**
	 * Creates an Authenticator which retrieves it's credentials (if necessary)
	 * from provider and sets the cache's availability for initial requests
	 * 
	 * @param provider A provider capable of retrieving HTTP credentials upon
	 * request.
	 * @param attemptCache True if the authenticator should attempt to provide
	 * cached credentials before requesting new ones from provider 
	 */
	public HttpAuthenticator(HttpCredentialProvider provider, boolean attemptCache) {
		this.provider = provider;
		this.attemptCache = attemptCache;
	}
	
	/**
	 * Receives a WWW-Authenticate challenge from connection (associated with the
	 * provided message) and attempts to handle the challenge.
	 * 
	 * @param connection The connection from which the challenge was issued.
	 * @param challenge The text of the WWW-Authenticate challenge
	 * @param message The message attempting to be sent
	 * @return An Authentication header to be added to the message on its next
	 * attempt, or null if this authenticator was unable to identify or handle
	 * the challenge.
	 */
	public String challenge(HttpConnection connection, String challenge, AuthenticatedHttpTransportMessage message) {
		String type = challenge.substring(0, challenge.indexOf(' '));
		String args = challenge.substring(challenge.indexOf(' ') + 1);
		if(type.equals("Digest")) {
			return digestResponse(connection,args, message);
		} else {
			return null;
		}
	}
	
	/**
	 * Retrieves a cached Authentication header which may work for the provided
	 * message, if one exists and caching is enabled. 
	 * @param message The message which is requesting potential authentication
	 * @return null if caching is disabled. <br/> null if the cache contains
	 * no appropriate auth headers <br/> and an Authentication header to be
	 * added to the message otherwise.
	 */
	public String checkCache(AuthenticatedHttpTransportMessage message) {
		if(attemptCache) {
			return AuthorizationCache.load().retrieveAuthHeader(message);
		} else {
			return null;
		}
	}
	
	/**
	 * Provides an Authentication header for a digest authentication challenge 
	 * 
	 * @param connection The connection from which the challenge was issued.
	 * @param args The arguments of the challenge.
	 * @param message The message attempting to be sent
	 * 
	 * @return An Authentication header to be added to the message on its next
	 * attempt, or null if this authenticator was unable to identify or handle
	 * the challenge.
	 */
	protected final String digestResponse(HttpConnection connection, String args, AuthenticatedHttpTransportMessage message) {
		
		//Parse out the parameters of the challenge
		Hashtable<String, String> params = AuthUtils.getQuotedParameters(args);
		
		//Acquire credentials for authentication (note: acquireCredentials may block, 
		//so this should be running in a non-ui path thread.
		if(provider == null || !provider.acquireCredentials()) {
			return null;
		}
		
		String username = provider.getUsername();
		
		//Generate HA1
		String HA1 = AuthUtils.MD5(username + ":" + params.get("realm") + ":" + provider.getPassword());
		
		//Create a response which will be used to create the header (and can be cached).
		DigestAuthResponse response = new DigestAuthResponse(connection.getURL(), HA1);
		
		
		String qop;
		
		//Determine the authentication scheme.
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
		
		//Read out the necessary parameters for the response. 
		String uri;
		
		if(params.containsKey("domain")) {
			uri = params.get("domain");
		} else {
			//TODO: This should get cut off at the end probably
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
		
		//Build the response header from these conditions
		String header = response.buildResponse(message);
		
		//Cache the response.
		AuthorizationCache.load().cache(response);
		
		return header;
	}
}
