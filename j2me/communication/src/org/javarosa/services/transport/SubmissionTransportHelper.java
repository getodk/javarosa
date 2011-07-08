/**
 * 
 */
package org.javarosa.services.transport;

import java.io.IOException;

import org.javarosa.core.model.SubmissionProfile;
import org.javarosa.core.model.instance.FormInstance;
import org.javarosa.core.services.transport.payload.IDataPayload;
import org.javarosa.model.xform.SMSSerializingVisitor;
import org.javarosa.model.xform.XFormSerializingVisitor;
import org.javarosa.model.xform.XPathReference;
import org.javarosa.services.transport.impl.simplehttp.SimpleHttpTransportMessage;

/**
 * @author ctsims
 *
 */
public class SubmissionTransportHelper {
	
	public static SubmissionProfile defaultPostSubmission(String url) {
		return new SubmissionProfile(new XPathReference("/"), "post", url, null);
	}
	
	public static TransportMessage createMessage(FormInstance instance, SubmissionProfile profile, boolean cacheable) throws IOException {
		//If there is a submission profile, we need to use the relevant portions.
		if(profile.getMethod().toLowerCase().equals("post")) {
			
			//URL
			String url = profile.getAction();
		
			IDataPayload payload = new XFormSerializingVisitor().createSerializedPayload(instance, profile.getRef());
			SimpleHttpTransportMessage message = new SimpleHttpTransportMessage(payload.getPayloadStream(),url);
			message.setCacheable(cacheable);
			message.setOpenRosaApiVersion(null);
			return message;
		} else if(profile.getMethod().toLowerCase().equals("smspush")) {
			
			//#if polish.api.wmapi

			//URL
			String phoneUri = profile.getAction();
		
			byte[] data = new SMSSerializingVisitor().serializeInstance(instance, profile.getRef());
			
			String payload = new String(data,"UTF-16BE");
			
			return new org.javarosa.services.transport.impl.sms.SMSTransportMessage(payload,phoneUri);	
			
			//#else
			//# throw new RuntimeException("SMS Messages not enabled on current device"); 
			//#endif
		}
		return null;
	}
}
