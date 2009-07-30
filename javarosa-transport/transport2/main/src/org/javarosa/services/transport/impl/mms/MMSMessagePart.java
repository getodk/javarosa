package org.javarosa.services.transport.impl.mms;

import java.io.IOException;
import java.io.InputStream;

import javax.wireless.messaging.MessagePart;
import javax.wireless.messaging.SizeExceededException;
///*This is to add Text*/
//String mimeType = "text/plain";
//String encoding = "UTF-8";
//String text = "Hello";
//byte[] contents = text.getBytes(encoding);
//mpart = new MessagePart(contents, 0, contents.length, mimeType, "id" +
//  counter, "contentLocation", encoding);
//
//counter ++;
//
///*This is to add Image*/
//String mimeType = "image/png";
//String image = "/hello.png";
//InputStream is = getClass().getResourceAsStream(image);
//byte[] contents = new byte[is.available()];
//is.read(contents);
//mpart = new MessagePart(contents, 0, contents.length,mimeType, "id" + 
//  counter,"contentLocation", null);
//parts.addElement(mpart);
//counter ++;
public class MMSMessagePart {

	public static MessagePart getMMSMessagePart(String contentId, String contentLocation,
			String mimeType) throws SizeExceededException, IOException {

		InputStream content = MMSMessagePart.class.getResourceAsStream(contentLocation);
		MessagePart messagePart = new MessagePart(content, mimeType, contentId,
				contentLocation, null);
		return messagePart;
	}
}
