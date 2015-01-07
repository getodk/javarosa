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
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;

import org.kxml2.io.KXmlSerializer;
import org.kxml2.kdom.Document;
import org.kxml2.kdom.Element;

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

	public static String elementToString(Element e){
		KXmlSerializer serializer = new KXmlSerializer();

		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		DataOutputStream dos = new DataOutputStream(bos);
		String s=null;
		try {
			serializer.setOutput(dos, null);
			e.write(serializer);
			serializer.flush();
			s = new String(bos.toByteArray(),"UTF-8");
			return s;
		}catch (UnsupportedEncodingException uce){
			uce.printStackTrace();
		} catch (Exception ex) {
			ex.printStackTrace();
			return null;
		}

		return null;

	}

	public static String getString(Document doc) {
		ByteArrayOutputStream bos = getStream(doc);

		byte[] byteArr = bos.toByteArray();
		char[] charArray = new char[byteArr.length];
		for (int i = 0; i < byteArr.length; i++)
			charArray[i] = (char) byteArr[i];

		return String.valueOf(charArray);
	}

	public static byte[] getUtfBytes(Document doc) {
		KXmlSerializer serializer = new KXmlSerializer();
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		try {
			Writer osw = new OutputStreamWriter(bos, "UTF-8");
			serializer.setOutput(osw);
			doc.write(serializer);
			serializer.flush();
			return bos.toByteArray();
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
}
