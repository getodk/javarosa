package org.javarosa.core.model;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import org.javarosa.core.model.utils.ExternalizableHelper;
import org.javarosa.core.util.Externalizable;


/**
 * Containts the connection request header details of user name, password,
 * and what action to execute.
 * 
 * @author Daniel Kayiwa
 *
 */
public class RequestHeader implements Externalizable{
	
	/** No status specified yet. */
	public static final byte ACTION_NONE = -1;
	
	/** Status to get a list of studies. */
	public static final byte ACTION_DOWNLOAD_STUDY_LIST = 2;
	
	/** Status to get a list of form definitions in a study. */
	public static final byte ACTION_DOWNLOAD_STUDY_FORMS = 3;
	
	/** Status to get a list of form definitions in a list of studies. */
	public static final byte ACTION_DOWNLOAD_STUDIES_FORMS = 4;
	
	/** Status to save a list of form data. */
	public static final byte ACTION_UPLOAD_DATA = 5;
		
	/** Status to download a list of users from the server. */
	public static final byte ACTION_DOWNLOAD_USERS = 7;
	
	/** Status to download a list of users and forms from the server. */
	public static final byte ACTION_DOWNLOAD_USERS_AND_FORMS = 11;
	
	/** The current status. This could be a request or return code status. */
	public byte action = ACTION_NONE;
		
	private String userName = Constants.EMPTY_STRING;
	private String password = Constants.EMPTY_STRING;
	
	/** Constructs a new communication parameter. */
	public RequestHeader(){
		super();
	}
	
	public byte getAction() {
		return action;
	}

	public void setAction(byte action) {
		this.action = action;
	}
	
	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}
		
	/**
	 * @see org.javarosa.core.util.util.db.Externalizable#writeExternal(java.io.DataOutputStream)
	 */
	public void writeExternal(DataOutputStream dos) throws IOException{
		dos.writeUTF(getUserName());
		dos.writeUTF(getPassword());
		dos.writeByte(getAction());
	}
	
	/**
	 * @see org.javarosa.core.util.util.db.Externalizable#readExternal(java.io.DataInputStream)
	 */
	public void readExternal(DataInputStream dis) throws IOException,InstantiationException,IllegalAccessException{
		if(!ExternalizableHelper.isEOF(dis)){
			setUserName(dis.readUTF());
			setPassword(dis.readUTF());
			setAction(dis.readByte());
		}
	}
}


/** The folder where to put form xml collected data in NON database mode. 
 * The files are named starting with form definition variable name and
 * ending with the form data id. 
	 * For each submitted set of form data, a new folder, 
	 * under the folder with the name of the study, under folder with name of user,
	 * is created having a name
 * which is composed of the date and time of submission.
 * This will most of the times ensure uniqueness of the files, but if a 
 * file of the same name is found in the same folder,
 * this form data is considerered not saved and the user will be required
 * to resolve this, as it can be dangerous to automatically overwrite such data.
 * */
//public static final String FORMS_DATA_FOLDER = "FormsDataFolder";
