package org.javarosa.clforms.storage;


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
