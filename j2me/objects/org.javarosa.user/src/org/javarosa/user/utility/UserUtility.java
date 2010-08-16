/**
 * 
 */
package org.javarosa.user.utility;

import org.javarosa.core.services.PropertyManager;
import org.javarosa.core.services.storage.IStorageIterator;
import org.javarosa.core.services.storage.IStorageUtility;
import org.javarosa.core.services.storage.StorageFullException;
import org.javarosa.core.services.storage.StorageManager;
import org.javarosa.core.util.PropertyUtils;
import org.javarosa.user.model.User;

/**
 * @author Clayton Sims
 * @date Mar 16, 2009 
 *
 */
public class UserUtility {
	//#ifdef admin.pw.default:defined
	//#=	private static final String defaultPassword = "${admin.pw.default}";
	//#else
			private static final String defaultPassword = "234";
	//#endif
	
	public static void populateAdminUser() {
		IStorageUtility users = StorageManager.getStorage(User.STORAGE_KEY);

		boolean adminFound = false;
		IStorageIterator ui = users.iterate();
		while (ui.hasMore()) {
			User user = (User)ui.nextRecord();
			if (User.ADMINUSER.equals(user.getUserType())) {
				adminFound = true;
				break;
			}
		}
		
		// There is no admin user to update, so add the user
		if(!adminFound) {
			User admin = new User("admin", defaultPassword, PropertyUtils.genGUID(25), User.ADMINUSER);
			admin.setUuid(PropertyManager._().getSingularProperty("DeviceID"));

			try {
				users.write(admin);
			} catch (StorageFullException e) {
				throw new RuntimeException("uh-oh, storage full [users]"); //TODO: handle this
			}
		}
	}
	
	public static User demoUser(boolean isAdmin) {
		return new User("demo", "", "demo"+PropertyUtils.genGUID(25), isAdmin ? User.ADMINUSER : User.DEMO_USER);
	}
}
