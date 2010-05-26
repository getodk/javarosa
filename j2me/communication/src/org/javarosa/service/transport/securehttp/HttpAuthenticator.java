package org.javarosa.service.transport.securehttp;

import java.io.IOException;
import java.io.InputStream;
import java.util.Hashtable;
import java.util.Random;
import java.util.Vector;

import javax.microedition.io.HttpConnection;

import org.javarosa.core.model.utils.DateUtils;
import org.javarosa.core.util.MD5;
import org.javarosa.core.util.MD5InputStream;
import org.javarosa.core.util.OrderedHashtable;

public abstract class HttpAuthenticator {
	
	private static final String QOP_UNSPECIFIED = "unspecified";
	private static final String QOP_AUTH = "auth";
	private static final String QOP_AUTH_INT = "auth-int";
	
	public final String challenge(HttpConnection connection, String challenge, AuthenticatedHttpTransportMessage message) {
		String type = challenge.substring(0, challenge.indexOf(' '));
		String args = challenge.substring(challenge.indexOf(' ') + 1);
		if(type.equals("Digest")) {
			return digestResponse(connection,args, message);
		} else {
			return customChallenge(connection, challenge, message);
		}
	}
	
	private String digestResponse(HttpConnection connection, String args, AuthenticatedHttpTransportMessage message) {
		Hashtable<String, String> params = AuthenticationUtils.getQuotedParameters(args);
		Hashtable<String, String> response = new OrderedHashtable(); 
		
		String username = getUsername();
		String HA1 = MD5(username + ":" + params.get("realm") + ":" + getPassword());
		String HA2 = null;
		
		String qop;
		
		if(!params.containsKey("qop")) {
			qop = QOP_UNSPECIFIED;
		} else {
			Vector<String> qops = DateUtils.split(params.get("qop"),",",false);
			if(qops.contains(QOP_AUTH_INT) && qops.contains(QOP_AUTH)) {
				//choose between auth-int and auth if both are available;
				qop = QOP_AUTH;
			} else if(qops.size() == 1) {
				if(qops.elementAt(0).equals(QOP_AUTH)) {
					qop = QOP_AUTH;
				} else if(qops.elementAt(0).equals(QOP_AUTH_INT)) {
					qop = QOP_AUTH_INT;
				} else {
					return null;
				}
			} else {
				//These are really the only possibilities...
				return null;
			}
		}
		
		String method = connection.getRequestMethod();
		
		String uri;
		if(params.containsKey("domain")) {
			uri = params.get("domain");
		} else {
			//TODO: This should get cut
			uri = connection.getURL();
		}
		
		String nonce = params.get("nonce");
		
		String opaque = params.get("opaque");
		
		response.put("username", quote(username));
		response.put("realm", quote(params.get("realm")));
		response.put("nonce", quote(nonce));
		response.put("uri", quote(uri));
		if(qop != QOP_UNSPECIFIED) {
			response.put("qop", qop);
		}
		
		if(qop != QOP_AUTH_INT) {
			HA2 = MD5(method + ":" + uri);
		} else {
			InputStream stream = message.getContentStream();
			String entityBody;
			if(stream == null){ 
				entityBody = MD5.toHex("".getBytes());
			} else {
				try {
					entityBody = new MD5InputStream(stream).getHashCode();
				} catch (IOException e) {
					//Problem calculating MD5 from content stream
					e.printStackTrace();
					return null;
				}
			}
			HA2 = MD5(method + ":" + uri);
		}
		
		if(qop == QOP_UNSPECIFIED) {
			//RFC 2069 Auth
			response.put("response", quote(MD5(HA1 + ":" + nonce + ":" + HA2)));
		} else {
			//TODO: Properly store and increment nonces...
			String nc = "00000001";
			response.put("nc",nc);
			
			//Generate client nonce
			String cnonce = getClientNonce();
			response.put("cnonce", quote(cnonce));
			
			//Calculate response
			response.put("response", quote(MD5(HA1 + ":" + nonce + ":" + nc + ":" + cnonce + ":" + qop + ":" + HA2)));
		}
		
		if(opaque != null) {
			response.put("opaque",quote(opaque));	
		}
		
		return "Digest " + AuthenticationUtils.encodeQuotedParameters(response);
	}
	
	private String quote(String input) {
		return '"' + input + '"';
	}
	
	private String getClientNonce() {
		Random r = new Random();
		byte[] b = new byte[8];
		for(int i = 0; i < b.length ; ++i) {
			b[i] = (byte)r.nextInt(256);
		}
		return MD5.toHex(b);
	}
	
	private String MD5(String input) { return MD5.toHex(new MD5(input.getBytes()).doFinal()); }
	
	protected abstract String getUsername();
	protected abstract String getPassword();
	
	public String customChallenge(HttpConnection connection, String challenge, AuthenticatedHttpTransportMessage message) {
		return null;
	}
}
