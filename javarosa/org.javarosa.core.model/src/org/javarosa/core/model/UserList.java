package org.javarosa.core.model;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Vector;

import org.javarosa.core.util.externalizable.Externalizable;
import org.javarosa.core.util.externalizable.ExternalizableHelperDeprecated;
import org.javarosa.core.util.externalizable.DeserializationException;
import org.javarosa.core.util.externalizable.PrototypeFactory;

/**
 * Contains a list of users.
 * 
 * @author Daniel
 *
 */
public class UserList implements Externalizable{

	private Vector users;
	
	public UserList(){
		
	}
	
	public UserList(Vector users){
		this.users = users;
	}
	
	public Vector getUsers() {
		return users;
	}

	public void setUsers(Vector users) {
		this.users = users;
	}

	public void addUser(User user){
		if(users == null)
			users = new Vector();
		users.addElement(user);
	}
	
	public void addUsers(Vector userList){
		if(userList != null){
			if(users == null)
				users = userList;
			else{
				for(int i=0; i<userList.size(); i++ )
					this.users.addElement(userList.elementAt(i));
			}
		}
	}
	
	public int size(){
		if(getUsers() == null)
			return 0;
		return getUsers().size();
	}
	
	public User getUser(int index){
		return (User)getUsers().elementAt(index);
	}
	
	/**
	 * @see org.javarosa.core.util.externalizable.util.db.Externalizable#readExternal(java.io.DataInputStream)
	 */
	public void readExternal(DataInputStream dis, PrototypeFactory pf) throws IOException, DeserializationException {
		if(!ExternalizableHelperDeprecated.isEOF(dis))
			setUsers(ExternalizableHelperDeprecated.readExternal(dis,new User().getClass()));
	}

	/**
	 * @see org.javarosa.core.util.externalizable.util.db.Externalizable#writeExternal(java.io.DataOutputStream)
	 */
	public void writeExternal(DataOutputStream dos) throws IOException {
		ExternalizableHelperDeprecated.writeExternal(getUsers(), dos);
	}
}
