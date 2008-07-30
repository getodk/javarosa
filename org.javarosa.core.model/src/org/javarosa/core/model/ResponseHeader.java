package org.javarosa.core.model;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import org.javarosa.core.model.utils.ExternalizableHelper;
import org.javarosa.core.util.Externalizable;

/**
 * Contains the header of a connection response.
 * 
 * @author Daniel
 *
 */
public class ResponseHeader implements Externalizable{

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
	 * @see org.javarosa.core.util.util.db.Externalizable#readExternal(java.io.DataInputStream)
	 */
	public void readExternal(DataInputStream dis) throws IOException {
		if(!ExternalizableHelper.isEOF(dis))
			setStatus(dis.readByte());
	}

	/**
	 * @see org.javarosa.core.util.util.db.Externalizable#writeExternal(java.io.DataOutputStream)
	 */
	public void writeExternal(DataOutputStream dos) throws IOException {
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
