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

/**
 * 
 */
package org.javarosa.core.log;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Date;

import org.javarosa.core.util.externalizable.DeserializationException;
import org.javarosa.core.util.externalizable.ExtUtil;
import org.javarosa.core.util.externalizable.Externalizable;
import org.javarosa.core.util.externalizable.PrototypeFactory;

/**
 * @author Clayton Sims
 * @date Apr 10, 2009 
 *
 */
public class LogEntry implements Externalizable {
	
	public static final String STORAGE_KEY = "LOG";
	
	public static String LOG_TYPE_APPLICATION = "APP";
	public static String LOG_TYPE_ACTIVITY = "ACTIVITY";
	
	Date time;
	
	String type;
	
	String message;
	
	/** 
	 * NOTE: For serialization purposes only
	 */
	public LogEntry() {
		
	}
	
	public LogEntry(String type, String message, Date time) {
		this.time = time;
		this.type = type;
		this.message = message;
	}

	/**
	 * @return the time
	 */
	public Date getTime() {
		return time;
	}

	/**
	 * @return the type
	 */
	public String getType() {
		return type;
	}

	/**
	 * @return the message
	 */
	public String getMessage() {
		return message;
	}

	/* (non-Javadoc)
	 * @see org.javarosa.core.util.externalizable.Externalizable#readExternal(java.io.DataInputStream, org.javarosa.core.util.externalizable.PrototypeFactory)
	 */
	public void readExternal(DataInputStream in, PrototypeFactory pf)
			throws IOException, DeserializationException {
		time = ExtUtil.readDate(in);
		type = ExtUtil.readString(in);
		message = ExtUtil.readString(in);
	}

	/* (non-Javadoc)
	 * @see org.javarosa.core.util.externalizable.Externalizable#writeExternal(java.io.DataOutputStream)
	 */
	public void writeExternal(DataOutputStream out) throws IOException {
		ExtUtil.writeDate(out, time);
		ExtUtil.writeString(out, ExtUtil.emptyIfNull(type));
		ExtUtil.writeString(out, ExtUtil.emptyIfNull(message));
	}

}
