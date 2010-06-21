/**
 * 
 */
package org.javarosa.user.utility;

import java.util.Stack;
import java.util.Vector;

import org.javarosa.core.model.instance.FormInstance;
import org.javarosa.core.model.instance.TreeElement;
import org.javarosa.core.model.utils.IInstanceProcessor;
import org.javarosa.core.services.storage.IStorageUtilityIndexed;
import org.javarosa.core.services.storage.StorageFullException;
import org.javarosa.core.services.storage.StorageManager;
import org.javarosa.user.model.User;
import org.javarosa.xform.util.XFormAnswerDataSerializer;

/**
 * The UserModel Processor is responsible for reading in a 
 * UserXML data block, parsing out the data elements relevant
 * to creating or updating user models, and handling those
 * elements in the J2ME Environment.
 * 
 * TODO: The failures are not very well handled
 * 
 * @author ctsims
 *
 */
public class UserModelProcessor implements IInstanceProcessor {
	
	private static final String XMLNS = "http://openrosa.org/user-registration";
	private static final String elementName = "registration";
	private  User user;
	
	XFormAnswerDataSerializer serializer = new XFormAnswerDataSerializer();

	/* (non-Javadoc)
	 * @see org.javarosa.core.model.utils.IInstanceProcessor#processInstance(org.javarosa.core.model.instance.FormInstance)
	 */
	public void processInstance(FormInstance tree) {
		Vector<TreeElement> registrationNodes = scrapeForRegistrations(tree);
		int failures = 0;
		int parsed = 0;
		
		String messages = "";
		
		for(TreeElement element : registrationNodes) {
			try {
				parseRegistration(element);
				parsed++;
			} catch (MalformedUserModelException e) {
				e.printStackTrace();
				messages += e.getMessage() + ", ";
				failures++;
			} catch (StorageFullException e) {
				e.printStackTrace();
				messages += e.getMessage() + ", ";
				failures++;
			}
		}
		
		if(failures > 0) {
			throw new RuntimeException("Errors while parsing or saving user model! " + 
					failures+ " models failed to parse, " + parsed + 
					" models succesfully parsed. Failure messages: " + 
					messages);
		}
	}
	
	public User getRegisteredUser() {
		return user;
	}
	
	private void parseRegistration(TreeElement head) throws MalformedUserModelException, StorageFullException {
		
		String username = getString(getChild(head, "username"),head);
		String password = getString(getChild(head, "password"),head);
		String uuid = getString(getChild(head,"uuid"),head);
		
		User u = getUserFromStorage(uuid);
		
		if(u == null) {
			u = new User(username,password, uuid);
		} else {
			if(!u.getPassword().equals(password)) {
				u.setPassword(password);
			}
		}
		
		TreeElement data = getChild(head,"user_data");
		
		for(int i = 0; i < data.getNumChildren(); ++i) {
			TreeElement datum = data.getChildAt(i);
			if(!datum.isRelevant()) {
				continue;
			}
			String keyName = datum.getAttributeValue(null,"key");
			if(keyName == null) {
				throw new MalformedUserModelException("User data for user" + username + "has a data element with no key");
			}
			
			u.setProperty(keyName, getString(datum, data));
		}
		
		storage().write(u);
		user = u; 
	}
	
	private IStorageUtilityIndexed storage() {
		return (IStorageUtilityIndexed)StorageManager.getStorage(User.STORAGE_KEY);
	}
	
	private User getUserFromStorage(String uuid) {
		IStorageUtilityIndexed storage = storage();
		Vector<Integer> IDs = storage.getIDsForValue(User.META_UID, uuid);
		if(!(IDs.size() > 0)) {
			return null;
		} else {
			return (User)storage.read(IDs.elementAt(0).intValue());
		}
	}
	
	private TreeElement getChild(TreeElement parent, String name) throws MalformedUserModelException{
		Vector<TreeElement> v = parent.getChildrenWithName(name);
		if(v.isEmpty()) {
			throw new MalformedUserModelException("Expected a node '" + name + "' in element: " + parent.getName());
		} else if(v.size() > 1) {
			throw new MalformedUserModelException("Too many children named: '" + name + "' in element: " + parent.getName());
		}
		
		TreeElement e = v.elementAt(0);
		
		if(!e.isRelevant()) {
			throw new MalformedUserModelException("Expected a node '" + name + "' in element: " + parent.getName());
		}
		
		return e;
	}
	
	private String getString(TreeElement element, TreeElement parent) throws MalformedUserModelException {

		
		if(element.getValue() == null) {
			throw new MalformedUserModelException("No data in Element: '" + element.getName() + "' with parent: " + parent.getName());
		}
		
		return element.getValue().uncast().getString();
	}
	
	
	
	private Vector<TreeElement> scrapeForRegistrations(FormInstance tree) {
		Stack<TreeElement> stack = new Stack<TreeElement>();
		Vector<TreeElement> registrations = new Vector<TreeElement>();
		
		stack.push(tree.getRoot());
		
		while(!stack.empty()) {
			TreeElement seeker = stack.pop();
			
			//TODO: Namespace support!
			if(seeker.getName().equals(elementName) && seeker.isRelevant()) {
				registrations.addElement(seeker);
			}
			
			for(int i = 0; i < seeker.getNumChildren(); ++i) {
				TreeElement child = seeker.getChildAt(i);
				//Skip non-relevant kids
				if(!child.isRelevant()) {
					continue;
				}
				
				//otherwise, put it in the stack!
				stack.push(child);
			}
		}
		return registrations;
	}
}
