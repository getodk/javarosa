package org.javarosa.user.model;


import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import org.javarosa.core.services.storage.utilities.IDRecordable;
import org.javarosa.core.util.externalizable.DeserializationException;
import org.javarosa.core.util.externalizable.Externalizable;
import org.javarosa.core.util.externalizable.PrototypeFactory;



public class User implements Externalizable, IDRecordable
{
	//USERTYPEs
	public static final String ADMINUSER = "admin";
	public static final String USERTYPE1 = "TLP";
	public static final String STANDARD = "standard";
	public static final String DEMO_USER = "demo_user";

	private int recordId = 0; //this objects ID in the RMS
	private String username;
	private String password;
	private String userType;
	private int id;
	private boolean rememberMe= false;

	private int [] formsApplied;


	public User ()
	{
		userType = STANDARD;
	}

	public User(String name, String passw, int id)
	{
		username = name;
		password = passw;
		userType =  STANDARD;
		rememberMe = false;
		this.id = id;
	}
	
	
	public User(String name, String passw, int id, String isAAdmin)
	{
		username = name;
		password = passw;
		if (isAAdmin.equals(ADMINUSER))
		{
			this.setUserType(ADMINUSER);
		}
		else if (isAAdmin.equals( STANDARD))
		{
			this.setUserType(STANDARD);
		}
		else if (isAAdmin.equals(USERTYPE1))
			this.setUserType( USERTYPE1);
		else
			System.out.println("while creating user, an invalid isAAdmin variable was passed in: options are \"STANDARD\" or \"ADMINSUER\" or \"USERTYPE1\"");
		this.id = id;
	}

	///fetch the value for the default user and password from the RMS
	public void readExternal(DataInputStream in, PrototypeFactory pf) throws IOException, DeserializationException {
		try {
			this.username = in.readUTF();
			this.password = in.readUTF();
			this.userType = in.readUTF();
			this.id = in.readInt();
			this.rememberMe = in.readBoolean();
		}
		catch (IOException ioe)
		{
			ioe.printStackTrace();
			System.out.println(ioe);
		}
	}

	public boolean isAdminUser()
	{
		if (userType.equals( ADMINUSER))
		return true;
		else return false;
	}

	public void writeExternal(DataOutputStream out) throws IOException {
		try {
			out.writeUTF(this.username);
	        out.writeUTF(this.password);
	        out.writeUTF(this.userType);
	        out.writeInt(this.id);
	        out.writeBoolean(this.rememberMe);

		} catch (IOException e) {
			throw new RuntimeException(e.getMessage()); //$NON-NLS-1$
		}

	}

	public String getUsername()
	{
		return username;
	}

	public String getPassword()
	{
		return password;
	}

	public void setRecordId(int recordId)
	{

		this.recordId = recordId;
	}

	public int getRecordId() {
		return recordId;
	}

	public String getUserType() {
		return userType;
	}
	
	public int getUserID() {
		return this.id;
	}

	public void setUserType(String userType) {
		this.userType = userType;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public boolean isRememberMe() {
		return rememberMe;
	}

	public void setRememberMe(boolean rememberMe) {
		this.rememberMe = rememberMe;
	}
	
	

}