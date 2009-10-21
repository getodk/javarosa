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

//TODO: Add verification of SMS address

package org.javarosa.communication.sms;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import org.javarosa.core.services.transport.ITransportDestination;
import org.javarosa.core.util.externalizable.DeserializationException;
import org.javarosa.core.util.externalizable.PrototypeFactory;

public class SmsTransportDestination implements ITransportDestination{

	private String smsAddress;
	
	public SmsTransportDestination() {
		super();
	}
	
	public SmsTransportDestination(String smsAddress) {
		this.smsAddress = smsAddress;
	}
	
	public String getSmsAddress() {
		return smsAddress;
	}

	public void readExternal(DataInputStream in, PrototypeFactory pf)
			throws IOException, DeserializationException {
		smsAddress = in.readUTF();
	}

	public void writeExternal(DataOutputStream out) throws IOException {
		out.writeUTF(smsAddress);
	}
	
	
}
