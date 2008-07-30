package org.javarosa.user.model;


import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import org.javarosa.core.services.storage.utilities.IDRecordable;
import org.javarosa.core.util.Externalizable;

//import javax.microedition.rms.RecordStoreException;



public class User implements Externalizable, IDRecordable
{
	//USERTYPEs
	public static final String ADMINUSER = "admin";
	public static final String USERTYPE1 = "TLP";
	public static final String STANDARD = "standard";

	private int recordId = 0; //this objects ID in the RMS
	private String username;
	private String password;
	private String userType;

	private int [] formsApplied;


	public User ()
	{
		userType = STANDARD;
	}

	public void setType (String typeIn)
	{
		userType = typeIn;
	}

	public String getType()
	{
		return userType;
	}
	public User(String name, String passw)
	{
		username = name;
		password = passw;
		userType =  STANDARD;
	}
	public User(String name, String passw, String isAAdmin)
	{
		username = name;
		password = passw;
		if (isAAdmin.equals( ADMINUSER))
		{
			this.setType( ADMINUSER);
		}
		else if (isAAdmin.equals( STANDARD))
		{
			this.setType( STANDARD);
		}
		else if (isAAdmin.equals( USERTYPE1))
			this.setType( USERTYPE1);
		else
			System.out.println("while creating user, an invalid isAAdmin variable was passed in: options are \"STANDARD\" or \"ADMINSUER\" or \"USERTYPE1\"");
	}

	///fetch the value for the default user and password from the RMS
	public void readExternal(DataInputStream in) throws IOException {
		try {
			this.username = in.readUTF();
			this.password = in.readUTF();
			this.userType = in.readUTF();
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

}