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

package org.javarosa.services.transport.http;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.Hashtable;

import org.javarosa.core.services.transport.IDataPayload;
import org.javarosa.core.services.transport.IDataPayloadVisitor;
import org.javarosa.core.services.transport.TransportMethod;
import org.javarosa.core.util.externalizable.DeserializationException;
import org.javarosa.core.util.externalizable.ExtUtil;
import org.javarosa.core.util.externalizable.ExtWrapMap;
import org.javarosa.core.util.externalizable.PrototypeFactory;

/**
 * A stream representing an HTTP header.
 * 
 * @author Clayton Sims
 * @date Dec 18, 2008 
 *
 */
public class HttpTransportHeader implements IDataPayload {

	/** String -> String **/
	Hashtable headers = new Hashtable();
	
	public HttpTransportHeader() {
	}
	
	public void addHeader(String key, String value) {
		headers.put(key, value);
	}
	
	/* (non-Javadoc)
	 * @see org.javarosa.core.services.transport.TransportHeaderStream#getTransportType()
	 */
	public int getTransportType() {
		return TransportMethod.HTTP_GCF;
	}
	
	public void readExternal(DataInputStream in, PrototypeFactory pf)
			throws IOException, DeserializationException {
		
		headers = (Hashtable)ExtUtil.read(in, new ExtWrapMap(String.class, String.class));
	}
	
	public void writeExternal(DataOutputStream out) throws IOException {
		ExtUtil.write(out, new ExtWrapMap(headers));
	}
	public Object accept(IDataPayloadVisitor visitor) {
		return null;
	}
	
	public String getPayloadId() {
		return null;
	}
	public InputStream getPayloadStream() {

		return new ByteArrayInputStream(getBytes());
	}
	
	private byte[] getBytes() {
		String header = "";
		Enumeration en = headers.keys();
		while(en.hasMoreElements()) {
			String key = (String)en.nextElement();
			String value = (String)headers.get(key);
			header += key + value + "\n";
		}
		header +="\n";
		return header.getBytes();
	}
	
	public int getPayloadType() {
		return 0;
	}

	/*
	 * (non-Javadoc)
	 * @see org.javarosa.core.services.transport.IDataPayload#getLength()
	 */
	public long getLength() {
		return getBytes().length;
	}
	
	public int getTransportId() { 
		return -1;
	}
}
