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

package org.javarosa.referral.properties;

import java.util.Vector;

import org.javarosa.core.services.properties.IPropertyRules;

public class ReferralPropertyRules implements IPropertyRules {
	public final static String REFERRALS_ENABLED_PROPERTY = "ref_enabled";
	
	public final static String REFERRALS_ENABLED = "e";
	public final static String REFERRALS_DISABLED = "d";

	public Vector allowableProperties() {
		Vector allowableProperties = new Vector();
		allowableProperties.addElement(REFERRALS_ENABLED_PROPERTY);
		return allowableProperties;
	}

	public Vector allowableValues(String propertyName) {
		if(REFERRALS_ENABLED_PROPERTY.equals(propertyName)) {
			Vector values = new Vector();
			values.addElement(REFERRALS_ENABLED);
			values.addElement(REFERRALS_DISABLED);
			return values;
		}
		return null;
	}

	public boolean checkPropertyAllowed(String propertyName) {
		return REFERRALS_ENABLED_PROPERTY.equals(propertyName);
	}

	public boolean checkPropertyUserReadOnly(String propertyName) {
		return false;
	}

	public boolean checkValueAllowed(String propertyName, String potentialValue) {
		if(REFERRALS_ENABLED_PROPERTY.equals(propertyName)) {
			if(REFERRALS_ENABLED.equals(potentialValue) ||
			   REFERRALS_DISABLED.equals(potentialValue)) {
				return true;
			}
		}
		return false;
	}

	public String getHumanReadableDescription(String propertyName) {
		if(REFERRALS_ENABLED_PROPERTY.equals(propertyName)) {
			return "Referrals Status";
		}
		return propertyName;
	}

	public String getHumanReadableValue(String propertyName, String value) {
		if(REFERRALS_ENABLED_PROPERTY.equals(propertyName)) {
			if(REFERRALS_ENABLED.equals(value)) {
				return "Enabled";
			} else if(REFERRALS_DISABLED.equals(value)) {
				return "Disabled";
			}
		}
		return null;
	}
    /*
     * (non-Javadoc)
     * @see org.javarosa.core.services.properties.IPropertyRules#handlePropertyChanges(java.lang.String)
     */
    public void handlePropertyChanges(String propertyName) {
    	//Is there anything to be done here? 
    }
}
