/**
 * 
 */
package org.javarosa.communication.http;

import java.util.Enumeration;

import org.javarosa.core.services.transport.ByteArrayPayload;
import org.javarosa.core.services.transport.DataPointerPayload;
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
	
	private String contentType;
	
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
		return visitIndividual(payload);
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
			contentType = "multipart/mixed; boundary='" + divider + "'";
			ret.addPayload(header);
		}
		HttpHeaderAppendingVisitor newVis = new HttpHeaderAppendingVisitor(divider);
		Enumeration en = payload.getPayloads().elements();
		while(en.hasMoreElements()) {
			IDataPayload child = (IDataPayload)en.nextElement();
			ret.addPayload((IDataPayload)child.accept(newVis));
		}
		HttpTransportHeader footer = new HttpTransportHeader();
		footer.addHeader("\n--", divider + "--");
		ret.addPayload(footer);
		return ret;
	}

	public Object visit(DataPointerPayload payload) {
		return visitIndividual(payload);
	}
	
	private Object visitIndividual(IDataPayload payload) {
		if(divider != null) {
			MultiMessagePayload message = new MultiMessagePayload();
			HttpTransportHeader header = new HttpTransportHeader();
			header.addHeader("\n--", divider);
			if(payload.getPayloadId() != null) {
				header.addHeader("Content-ID: ", payload.getPayloadId());
			}
			switch(payload.getPayloadType()) {
			case IDataPayload.PAYLOAD_TYPE_JPG:
				header.addHeader("Content-type: ", getContentTypeFromId(payload.getPayloadType()));
				header.addHeader("Content-transfer-encoding: ", "binary");
				break;
			default: 
				header.addHeader("Content-type: ", getContentTypeFromId(payload.getPayloadType()));
			}
			

			
			message.addPayload(header);
			message.addPayload(payload);
			return message;
		}
		else {
			contentType = getContentTypeFromId(payload.getPayloadType());
			return payload;
		}
	}
	
	public String getOverallContentType() {
		return contentType;
	}
	
	private String getContentTypeFromId(int id) {
		switch(id) {
		case IDataPayload.PAYLOAD_TYPE_TEXT:
			return "text/plain";
		case IDataPayload.PAYLOAD_TYPE_XML:
			return "text/xml";
		case IDataPayload.PAYLOAD_TYPE_JPG:
			return "image/jpeg";
			//TODO: Handle this
			//header.addHeader("Content-transfer-encoding: ", "binary");
		}
		return "text/plain";
	}
}
