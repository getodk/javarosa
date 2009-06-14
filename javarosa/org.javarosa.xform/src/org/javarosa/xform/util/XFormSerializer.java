package org.javarosa.xform.util;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;

import org.kxml2.io.KXmlSerializer;
import org.kxml2.kdom.Document;

/* this is just a big dump of serialization-related code */

/* basically, anything that didn't belong in XFormParser */

public class XFormSerializer {
	
	public static ByteArrayOutputStream getStream(Document doc) {
		KXmlSerializer serializer = new KXmlSerializer();
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		DataOutputStream dos = new DataOutputStream(bos);

		try {
			serializer.setOutput(dos, null);
			doc.write(serializer);
			serializer.flush();
			return bos;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	public static String getString(Document doc) {
		ByteArrayOutputStream bos = getStream(doc);

		byte[] byteArr = bos.toByteArray();
		char[] charArray = new char[byteArr.length];
		for (int i = 0; i < byteArr.length; i++)
			charArray[i] = (char) byteArr[i];

		return String.valueOf(charArray);
	}
}
