package org.javarosa.services.transport.impl.mms;

import java.io.IOException;
import java.io.InputStream;

import javax.wireless.messaging.MessagePart;
import javax.wireless.messaging.SizeExceededException;

public class MMSMessagePart {

	public static MessagePart getMMSMessagePart(String contentId, String contentLocation,
			String mimeType) throws SizeExceededException, IOException {

		InputStream content = MMSMessagePart.class.getResourceAsStream(contentLocation);
		MessagePart messagePart = new MessagePart(content, mimeType, contentId,
				contentLocation, null);
		return messagePart;
	}
}
