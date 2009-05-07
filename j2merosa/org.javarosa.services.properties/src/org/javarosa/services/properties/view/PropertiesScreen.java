package org.javarosa.services.properties.view;

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

import javax.microedition.lcdui.ChoiceGroup;
import javax.microedition.lcdui.Display;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.Form;
import javax.microedition.lcdui.Item;
import javax.microedition.lcdui.StringItem;
import javax.microedition.lcdui.TextField;
import javax.microedition.rms.RecordStore;
import javax.microedition.rms.RecordStoreException;

import org.javarosa.core.JavaRosaServiceProvider;
import org.javarosa.core.api.IView;
import org.javarosa.core.services.PropertyManager;
import org.javarosa.core.services.IPropertyManager;
import org.javarosa.core.services.properties.IPropertyRules;
import org.javarosa.core.services.storage.utilities.RMSUtility;

public class PropertiesScreen extends Form implements IView{

    Hashtable itemChoices;

    Hashtable changes;
    
    /** item -> String **/
    Hashtable itemForPropertyName;

    Display currentDisplay;

    Displayable currentScreen;

    IPropertyManager propertyManager;

    public PropertiesScreen(IPropertyManager propertyManager) {
        super("Properties");
        itemChoices = new Hashtable();
        changes = new Hashtable();
        itemForPropertyName = new Hashtable();
        this.propertyManager = propertyManager;

        populateProperties();
        addRMSInfoJ2MEOnly();
    }

    public void showPropertiesScreen(Display currentDisplay,
            Displayable currentScreen) {
        this.currentDisplay = currentDisplay;
        this.currentScreen = currentScreen;
        currentDisplay.setCurrent(this);
    }

    private void populateProperties() {
    	Vector readOnlys = new Vector();
    	
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
						if (propValues.size() <= 1 && propValues.size()>0) {
								TextField input = new TextField(
										rules
												.getHumanReadableDescription(propertyName),
										(String) propValues.elementAt(0), 50,
										TextField.ANY);
								
								itemForPropertyName.put(input, propertyName);
							if (rules.checkPropertyUserReadOnly(propertyName)) {
								input.setConstraints(TextField.UNEDITABLE);
								readOnlys.addElement(input);
							} else {
								this.append(input);
							}

						}
					}
					else {
							TextField input = new TextField(rules.getHumanReadableDescription(propertyName),
									"", 50,
									TextField.ANY);
							itemForPropertyName.put(input, propertyName);
							if(rules.checkPropertyUserReadOnly(propertyName)) {
								input.setConstraints(TextField.UNEDITABLE);
								readOnlys.addElement(input);
							} else {
								this.append(input);
							}
					}
				}
			}
		}
		
		Enumeration enden = readOnlys.elements();
		while(enden.hasMoreElements()) {
			Item currentElement = (Item)enden.nextElement();
			this.append(currentElement);
		}
		
	}

    private void addRMSInfoJ2MEOnly () {
    	try {
	    	String[] stores = RecordStore.listRecordStores();
			for (int i = 0; i < stores.length; i++) {
				String rmsName = stores[i];
				RecordStore rms = RecordStore.openRecordStore(rmsName, false);
				int size = rms.getSize();
				int avail = rms.getSizeAvailable();
				int numRecs = rms.getNumRecords();
				int perRecord = (numRecs == 0 ? -1 : size / numRecs);
				
				this.append(new StringItem(null, rmsName));
				this.append(new StringItem(null, "Used: " + formatBytes(size)));
				this.append(new StringItem(null, "Records: " + numRecs + (numRecs > 0 ? " (" + formatBytes(perRecord) + " per record)" : "")));
				this.append(new StringItem(null, "Available: " + formatBytes(avail)));
			}
    	} catch (RecordStoreException rse) { }
    }
    
    private void addRMSInfo() {
    	Vector stores = JavaRosaServiceProvider.instance().getStorageManager().getRMSStorageProvider().getUtilityNames();
        int consumedSpace[] = new int[stores.size()];
        int availableSpace[] = new int[stores.size()];
        
        Enumeration names = stores.elements();
		int i = 0;

		while (names.hasMoreElements()) {
			String name = (String) names.nextElement();
			RMSUtility utility = JavaRosaServiceProvider.instance()
					.getStorageManager().getRMSStorageProvider().getUtility(
							name);

			consumedSpace[i] = (int) utility.getConsumedSpace();
			availableSpace[i] = (int) utility.getAvailableSpace();
			++i;
		}

		String devID = JavaRosaServiceProvider.instance().getPropertyManager()
				.getSingularProperty("DeviceID");
		this.append(new StringItem(null, "Device ID: " + devID));
		for (i = 0; i < stores.size(); i++) {
			this.append(new StringItem(null, (String) stores.elementAt(i)
					.toString()));
			this.append(new StringItem(null, "Available: "
					+ formatBytes(availableSpace[i])));
			this.append(new StringItem(null, "Used: "
					+ formatBytes(consumedSpace[i])));
		}

	}

    private String formatBytes (int bytes) {   	
    	int NUM_DIGITS = 2;
    	
    	if (bytes <= 0)
    		return "err";
    	
    	double kb = bytes / 1024.;
    	String str = String.valueOf(kb);
    	if (str.indexOf(".") != -1)
    		str = str.substring(0, Math.min(str.indexOf(".") + 1 + NUM_DIGITS, str.length()));
    	return str + " KB";
    }
    
//	private String formatBytes(int bytes) {
//        if (bytes == -1)
//            return "error";
//
//        String kbytes = "";
//        //Check for overflow
//        if(bytes*10 < bytes) {
//        	//Handle this case with low precision
//        	int kb  = bytes / 1024;
//        	kbytes = String.valueOf(kb);
//        }
//        else if(bytes*100 < bytes){
//        	int kb = (bytes*10)/1024;
//        	kbytes = String.valueOf(kb);
//        	if(kbytes.length() >= 2) {
//        		kbytes = kbytes.substring(0,kbytes.length()-2) + "." + kbytes.charAt(kbytes.length()-1);
//        	}
//        	else {
//        		kbytes = "0." + kbytes;
//        	}
//        }
//        else {
//        	int kb = (bytes*100)/1024;
//        	kbytes = String.valueOf(kb);
//        	if(kbytes.length() >= 3) {
//        		kbytes = kbytes.substring(0,kbytes.length()-3) + "." + kbytes.substring(kbytes.length()-2, kbytes.length()-1);
//        	}
//        	else if(kbytes.length() >= 2) {
//        		kbytes = "0." + kbytes;
//        	}
//        	else {
//        		kbytes = "0.0" + kbytes;
//        	}
//        }
//
//        return kbytes + " KB";
//    }

    public Hashtable getItemChoices() {
    	return itemChoices;
    }
    
    public String nameFromItem(Item item) {
    	return (String)itemForPropertyName.get(item);
    }
	public Object getScreenObject() {
		return this;
	}
}