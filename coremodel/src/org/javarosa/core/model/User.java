package org.javarosa.core.model;


import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import org.javarosa.util.db.Persistent;
import org.javarosa.util.db.PersistentHelper;


/**
 * A user of the system.
 * 
 * @author Daniel
 *
 */
public class User implements Persistent{

	private int userId;
	private String name;
	private String password;
	private String salt;
	
	public User(){
		
	}
	
	public User(int userId,String name, String password, String salt){
		this.userId = userId;
		this.name = name;
		this.password = password;
		this.salt = salt;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}
	
	public String getSalt() {
		return salt;
	}

	public void setSalt(String salt) {
		this.salt = salt;
	}

	public int getUserId() {
		return userId;
	}

	public void setUserId(int userId) {
		this.userId = userId;
	}
	
	/** 
	 * Reads the User object from the supplied stream.
	 * 
	 * @param dis - the stream to read from.
	 * @throws IOException
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 */
	public void read(DataInputStream dis) throws IOException, InstantiationException, IllegalAccessException {
		if(!PersistentHelper.isEOF(dis)){
			setUserId(dis.readInt());
			setName(dis.readUTF());
			setPassword(dis.readUTF());
			setSalt(dis.readUTF());
		}
	}

	/** 
	 * Writes the User object to the supplied stream.
	 * 
	 * @param dos - the stream to write to.
	 * @throws IOException
	 */
	public void write(DataOutputStream dos) throws IOException {
		dos.writeInt(getUserId());
		dos.writeUTF(getName());
		dos.writeUTF(getPassword());
		dos.writeUTF(getSalt());
	}
}
