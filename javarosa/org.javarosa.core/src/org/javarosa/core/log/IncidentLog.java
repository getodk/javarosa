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
public class IncidentLog implements Externalizable {
	
	Date time;
	
	String type;
	
	String message;
	
	/** 
	 * NOTE: For serialization purposes only
	 */
	public IncidentLog() {
		
	}
	
	public IncidentLog(String type, String message, Date time) {
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
		ExtUtil.writeString(out, type);
		ExtUtil.writeString(out, message);
	}

}
