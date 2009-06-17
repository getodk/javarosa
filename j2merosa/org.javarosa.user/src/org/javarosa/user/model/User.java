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

package org.javarosa.user.model;


import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Enumeration;
import java.util.Hashtable;

import org.javarosa.core.model.instance.DataModelTree;
import org.javarosa.core.model.instance.TreeElement;
import org.javarosa.core.model.instance.TreeReference;
import org.javarosa.core.model.util.restorable.Restorable;
import org.javarosa.core.model.util.restorable.RestoreUtils;
import org.javarosa.core.services.storage.utilities.IDRecordable;
import org.javarosa.core.util.externalizable.DeserializationException;
import org.javarosa.core.util.externalizable.ExtUtil;
import org.javarosa.core.util.externalizable.ExtWrapMap;
import org.javarosa.core.util.externalizable.Externalizable;
import org.javarosa.core.util.externalizable.PrototypeFactory;



public class User implements Externalizable, IDRecordable, Restorable
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
	
	/** String -> String **/
	private Hashtable properties = new Hashtable(); 

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
			this.properties = (Hashtable)ExtUtil.read(in, new ExtWrapMap(String.class, String.class));
			this.recordId = in.readInt();
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
			ExtUtil.write(out, new ExtWrapMap(properties));
			out.writeInt(recordId);

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
	
	public void setProperty(String key, String val) {
		this.properties.put(key, val);
	} 
	
	public String getProperty(String key) {
		return (String)this.properties.get(key);
	}
	
	public String getRestorableType() {
		return "user";
	}

	public DataModelTree exportData() {
		DataModelTree dm = RestoreUtils.createDataModel(this);
		RestoreUtils.addData(dm, "name", username);
		RestoreUtils.addData(dm, "pass", password);
		RestoreUtils.addData(dm, "type", userType);
		RestoreUtils.addData(dm, "user-id", new Integer(id));
		RestoreUtils.addData(dm, "remember", new Boolean(rememberMe));		
		
		for (Enumeration e = properties.keys(); e.hasMoreElements(); ) {
			String key = (String)e.nextElement();
			RestoreUtils.addData(dm, "other/" + key, properties.get(key));
		}
				
		return dm;
	}

	public void templateData(DataModelTree dm, TreeReference parentRef) {
		RestoreUtils.applyDataType(dm, "name", parentRef, String.class);
		RestoreUtils.applyDataType(dm, "pass", parentRef, String.class);
		RestoreUtils.applyDataType(dm, "type", parentRef, String.class);
		RestoreUtils.applyDataType(dm, "user-id", parentRef, Integer.class);
		RestoreUtils.applyDataType(dm, "remember", parentRef, Boolean.class);
		
		// other/* defaults to string
	}

	public void importData(DataModelTree dm) {
		username = (String)RestoreUtils.getValue("name", dm);
		password = (String)RestoreUtils.getValue("pass", dm);
		userType = (String)RestoreUtils.getValue("type", dm);
		id = ((Integer)RestoreUtils.getValue("user-id", dm)).intValue();
		rememberMe = RestoreUtils.getBoolean(RestoreUtils.getValue("remember", dm));
        
        TreeElement e = dm.resolveReference(RestoreUtils.absRef("other", dm));
        if (e != null) {
            for (int i = 0; i < e.getNumChildren(); i++) {
            	TreeElement child = (TreeElement)e.getChildren().elementAt(i);
            	String name = child.getName();
            	Object value = RestoreUtils.getValue("other/" + name, dm);
            	if (value != null){
            	    properties.put(name, value);
            	}
            }
        }
	}
	
}