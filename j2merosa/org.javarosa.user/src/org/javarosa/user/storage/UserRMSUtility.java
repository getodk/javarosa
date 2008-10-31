package org.javarosa.user.storage;

import org.javarosa.core.services.storage.utilities.RMSUtility;
import org.javarosa.user.model.User;


public class UserRMSUtility  extends RMSUtility {

	public UserRMSUtility (String  name )
	{
		super(name, RMSUtility.RMS_TYPE_STANDARD);
	}


	 public void writeToRMS(User user)
	 {

	        super.writeToRMS(user, null);
	 }


}
