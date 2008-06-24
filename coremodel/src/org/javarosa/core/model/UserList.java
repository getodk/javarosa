package org.javarosa.core.model;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Vector;

import org.javarosa.core.model.User;
import org.javarosa.util.db.Persistent;
import org.javarosa.util.db.PersistentHelper;

/**
 * Contains a list of users.
 * 
 * @author Daniel
 *
 */
public class UserList implements Persistent{

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
	 * @see org.javarosa.util.db.Persistent#read(java.io.DataInputStream)
	 */
	public void read(DataInputStream dis) throws IOException, InstantiationException, IllegalAccessException {
		if(!PersistentHelper.isEOF(dis))
			setUsers(PersistentHelper.read(dis,new User().getClass()));
	}

	/**
	 * @see org.javarosa.util.db.Persistent#write(java.io.DataOutputStream)
	 */
	public void write(DataOutputStream dos) throws IOException {
		PersistentHelper.write(getUsers(), dos);
	}
}
