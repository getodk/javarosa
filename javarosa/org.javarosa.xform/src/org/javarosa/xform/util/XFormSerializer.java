/*
 * Copyright (C) 2009 JavaRosa
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

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
