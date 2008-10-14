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
}
