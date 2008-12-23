/**
 * 
 */
package org.javarosa.communication.http;

import java.util.Enumeration;

import org.javarosa.core.api.Constants;
import org.javarosa.core.services.transport.ByteArrayPayload;
import org.javarosa.core.services.transport.IDataPayload;
import org.javarosa.core.services.transport.IDataPayloadVisitor;
import org.javarosa.core.services.transport.MultiMessagePayload;

/**
 * @author Clayton Sims
 * @date Dec 18, 2008 
 *
 */
public class HttpHeaderAppendingVisitor implements IDataPayloadVisitor {

	private boolean top = false;
	private String divider;
	
	public HttpHeaderAppendingVisitor() {
		top = true;
	}
	
	private HttpHeaderAppendingVisitor(String divider) {
		this.divider = divider;
		this.top = false;
	}
	
	/* (non-Javadoc)
	 * @see org.javarosa.core.services.transport.IDataPayloadVisitor#visit(org.javarosa.core.services.transport.ByteArrayPayload)
	 */
	public Object visit(ByteArrayPayload payload) {
		if(divider != null) {
			MultiMessagePayload message = new MultiMessagePayload();
			HttpTransportHeader header = new HttpTransportHeader();
			header.addHeader("--", divider);
			if(payload.getPayloadId() != null) {
				header.addHeader("Content-ID: ", payload.getPayloadId());
			}
			switch(payload.getPayloadType()) {
			case Constants.PAYLOAD_TYPE_TEXT:
				header.addHeader("Content-type: ", "text/plain");
				break;
			case Constants.PAYLOAD_TYPE_JPG:
				header.addHeader("Content-type: ", "image/jpg");
				header.addHeader("Content-transfer-encoding: ", "binary");
				break;
			}
			
			message.addPayload(header);
			message.addPayload(payload);
			return message;
		}
		else {
			return payload;
		}
	}

	/* (non-Javadoc)
	 * @see org.javarosa.core.services.transport.IDataPayloadVisitor#visit(org.javarosa.core.services.transport.MultiMessagePayload)
	 */
	public Object visit(MultiMessagePayload payload) {
		MultiMessagePayload ret = new MultiMessagePayload();
		if(top) {
			//TODO: Create a reasonable divider, and 
			divider = "newdivider";
			HttpTransportHeader header = new HttpTransportHeader();
			header.addHeader("MIME-version: ", "1.0");
			header.addHeader("Content-type: ", "multipart/mixed; boundary='" + divider + "'");
			ret.addPayload(header);
		}
		HttpHeaderAppendingVisitor newVis = new HttpHeaderAppendingVisitor(divider);
		Enumeration en = payload.getPayloads().elements();
		while(en.hasMoreElements()) {
			IDataPayload child = (IDataPayload)en.nextElement();
			ret.addPayload((IDataPayload)child.accept(newVis));
		}
		return ret;
	}

}
