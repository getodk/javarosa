package org.javarosa.reminders.properties;

import java.util.Vector;

import org.javarosa.core.services.properties.IPropertyRules;

public class ReminderPropertyRules implements IPropertyRules {
	public final static String REMINDERS_ENABLED_PROPERTY = "rem_enabled";
	
	public final static String REMINDERS_ENABLED = "e";
	public final static String REMINDERS_DISABLED = "d";

	public Vector allowableProperties() {
		Vector allowableProperties = new Vector();
		allowableProperties.addElement(REMINDERS_ENABLED_PROPERTY);
		return allowableProperties;
	}

	public Vector allowableValues(String propertyName) {
		if(REMINDERS_ENABLED_PROPERTY.equals(propertyName)) {
			Vector values = new Vector();
			values.addElement(REMINDERS_ENABLED);
			values.addElement(REMINDERS_DISABLED);
			return values;
		}
		return null;
	}

	public boolean checkPropertyAllowed(String propertyName) {
		return REMINDERS_ENABLED_PROPERTY.equals(propertyName);
	}

	public boolean checkPropertyUserReadOnly(String propertyName) {
		return false;
	}

	public boolean checkValueAllowed(String propertyName, String potentialValue) {
		if(REMINDERS_ENABLED_PROPERTY.equals(propertyName)) {
			if(REMINDERS_ENABLED.equals(potentialValue) ||
			   REMINDERS_DISABLED.equals(potentialValue)) {
				return true;
			}
		}
		return false;
	}

	public String getHumanReadableDescription(String propertyName) {
		if(REMINDERS_ENABLED_PROPERTY.equals(propertyName)) {
			return "Reminder Prompts";
		}
		return propertyName;
	}

	public String getHumanReadableValue(String propertyName, String value) {
		if(REMINDERS_ENABLED_PROPERTY.equals(propertyName)) {
			if(REMINDERS_ENABLED.equals(value)) {
				return "Enabled";
			} else if(REMINDERS_DISABLED.equals(value)) {
				return "Disabled";
			}
		}
		return null;
	}
}
