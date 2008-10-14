package org.javarosa.services.properties.view;

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

import javax.microedition.lcdui.ChoiceGroup;
import javax.microedition.lcdui.Display;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.Form;
import javax.microedition.lcdui.Item;
import javax.microedition.lcdui.ItemStateListener;
import javax.microedition.lcdui.StringItem;
import javax.microedition.lcdui.TextField;
import javax.microedition.rms.RecordStore;
import javax.microedition.rms.RecordStoreException;

import org.javarosa.core.JavaRosaServiceProvider;
import org.javarosa.core.services.PropertyManager;
import org.javarosa.core.services.properties.IPropertyRules;

public class PropertiesScreen extends Form{

    Hashtable itemChoices;

    Hashtable changes;
    
    /** item -> String **/
    Hashtable itemForPropertyName;

    Display currentDisplay;

    Displayable currentScreen;

    PropertyManager propertyManager;

    public PropertiesScreen(PropertyManager propertyManager) {
        super("Properties");
        itemChoices = new Hashtable();
        changes = new Hashtable();
        itemForPropertyName = new Hashtable();
        this.propertyManager = propertyManager;

        populateProperties();
        addRMSInfo();
    }

    public void showPropertiesScreen(Display currentDisplay,
            Displayable currentScreen) {
        this.currentDisplay = currentDisplay;
        this.currentScreen = currentScreen;
        currentDisplay.setCurrent(this);
    }

    private void populateProperties() {
		Vector rulesSets = propertyManager
				.getRules();
		Enumeration en = rulesSets.elements();
		while (en.hasMoreElements()) {
			IPropertyRules rules = (IPropertyRules) en.nextElement();
			Vector properties = rules.allowableProperties();

			for (int i = 0; i < properties.size(); ++i) {
				String propertyName = (String) properties.elementAt(i);
				Vector options = rules.allowableValues(propertyName);
				// Check to see whether this property has a dynamic rules list
				if (options.size() == 1) {
					String option = (String) options.elementAt(0);
					if (rules.checkPropertyAllowed(option)) {
						// this is a Dynamic property list, replace options
						options = propertyManager.getProperty(option);
					}
				}
				// If there are no options, it is an internal system's variable.
				// Don't touch
				if (options.size() != 0) {
					Vector propValues = propertyManager.getProperty(propertyName);
					// We can easily add the functionality to use multiple
					// possible choices here but
					// for now, we'll stick with single-selection properties
					if (propValues == null || propValues.size() == 1) {
						// Pull the property's current value
						String currentSelection = "";
						if(propValues != null) {
							currentSelection = (String) propValues.elementAt(0);
						}

						// Create the UI Elements
						ChoiceGroup newChoiceGroup = new ChoiceGroup(
								rules.getHumanReadableDescription(propertyName), ChoiceGroup.EXCLUSIVE);
						itemChoices.put(newChoiceGroup, options);
						itemForPropertyName.put(newChoiceGroup, propertyName);

						// Seek through to find the property in the list of
						// potentials
						int selindex = 0;
						for (int j = 0; j < options.size(); ++j) {
							String option = (String) options.elementAt(j);
							if (option.equals(currentSelection)) {
								selindex = j;
							}
							newChoiceGroup.append(rules.getHumanReadableValue(propertyName, option), null);
						}

						// Finish it all up
						newChoiceGroup.setSelectedIndex(selindex, true);
						this.append(newChoiceGroup);
					}
				} else {
					Vector propValues = propertyManager.getProperty(propertyName);
					// We can easily add the functionality to use multiple
					// possible choices here but
					// for now, we'll stick with single-selection properties
					if (propValues != null) {
						if (propValues.size() <= 1
								&& !rules.checkPropertyUserReadOnly(propertyName)) {
							TextField input = new TextField(rules.getHumanReadableDescription(propertyName),
									(String) propValues.elementAt(0), 50,
									TextField.ANY);
							this.append(input);
							itemForPropertyName.put(input, propertyName);

						}
					}
					else {
						if(!rules.checkPropertyUserReadOnly(propertyName)) {
							TextField input = new TextField(rules.getHumanReadableDescription(propertyName),
									"", 50,
									TextField.ANY);
							this.append(input);
							itemForPropertyName.put(input, propertyName);
						}
					}
				}
			}
		}
	}

    private void addRMSInfo() {

        String[] recordStores = RecordStore.listRecordStores();
        int consumedSpace[] = new int[recordStores.length];
        int availableSpace[] = new int[recordStores.length];

        for (int i = 0; i < recordStores.length; i++) {
            try {
                RecordStore r = RecordStore.openRecordStore(recordStores[i],
                        false);
                consumedSpace[i] = r.getSize();
                availableSpace[i] = r.getSizeAvailable();
                // really should close the recordstore here, but we don't do it
                // anywhere else and it might introduce bugs
            } catch (RecordStoreException e) {
                consumedSpace[i] = -1;
            }
        }

        String devID = JavaRosaServiceProvider.instance().getPropertyManager().getSingularProperty("DeviceID");
        this.append(new StringItem(null, "Device ID: "+devID));
        for (int i = 0; i < recordStores.length; i++) {
        	this.append(new StringItem(null, recordStores[i]));
        	this.append(new StringItem(null,"Available: "+ formatBytes(availableSpace[i])));
            this.append(new StringItem(null,"Used: " + formatBytes(consumedSpace[i])));
        }

    }

    private String formatBytes(int bytes) {
        if (bytes == -1)
            return "error";

        String kbytes = "";
        //Check for overflow
        if(bytes*10 < bytes) {
        	//Handle this case with low precision
        	int kb  = bytes / 1024;
        	kbytes = String.valueOf(kb);
        }
        else if(bytes*100 < bytes){
        	int kb = (bytes*10)/1024;
        	kbytes = String.valueOf(kb);
        	if(kbytes.length() >= 2) {
        		kbytes = kbytes.substring(0,kbytes.length()-2) + "." + kbytes.charAt(kbytes.length()-1);
        	}
        	else {
        		kbytes = "0." + kbytes;
        	}
        }
        else {
        	int kb = (bytes*100)/1024;
        	kbytes = String.valueOf(kb);
        	if(kbytes.length() >= 3) {
        		kbytes = kbytes.substring(0,kbytes.length()-3) + "." + kbytes.substring(kbytes.length()-2, kbytes.length()-1);
        	}
        	else if(kbytes.length() >= 2) {
        		kbytes = "0." + kbytes;
        	}
        	else {
        		kbytes = "0.0" + kbytes;
        	}
        }

        return kbytes + " KB";
    }

    public Hashtable getItemChoices() {
    	return itemChoices;
    }
    
    public String nameFromItem(Item item) {
    	return (String)itemForPropertyName.get(item);
    }
}