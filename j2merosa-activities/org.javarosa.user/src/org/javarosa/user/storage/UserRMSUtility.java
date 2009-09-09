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

	 public static String getUtilityName() {
		 return "User RMS Utility";
	 }

}
