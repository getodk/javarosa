package org.javarosa.core.model;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import org.javarosa.util.db.Persistent;
import org.javarosa.util.db.PersistentHelper;

/**
 * Contains the header of a connection response.
 * 
 * @author Daniel
 *
 */
public class ResponseHeader implements Persistent{

	/** Problems occured during execution of the request. */
	public static final byte STATUS_ERROR = 0;
	
	/** Request completed successfully. */
	public static final byte STATUS_SUCCESS = 1;
	
	private byte status = STATUS_ERROR;
	
	public ResponseHeader(){
		
	}
	
	public ResponseHeader(byte status){
		setStatus(status);
	}
	
	/**
	 * @see org.javarosa.util.db.Persistent#read(java.io.DataInputStream)
	 */
	public void read(DataInputStream dis) throws IOException {
		if(!PersistentHelper.isEOF(dis))
			setStatus(dis.readByte());
	}

	/**
	 * @see org.javarosa.util.db.Persistent#write(java.io.DataOutputStream)
	 */
	public void write(DataOutputStream dos) throws IOException {
		dos.writeByte(getStatus());
	}

	public byte getStatus() {
		return status;
	}

	public void setStatus(byte status) {
		this.status = status;
	}
	
	public boolean isSuccess(){
		return getStatus() == STATUS_SUCCESS;
	}
}
