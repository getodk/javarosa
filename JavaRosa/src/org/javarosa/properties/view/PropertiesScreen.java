//#if polish.usePolishGui
package org.javarosa.properties.view;

import java.util.Enumeration;
import java.util.Vector;

import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.Display;
import javax.microedition.lcdui.Displayable;
import javax.microedition.rms.RecordStore;
import javax.microedition.rms.RecordStoreException;

import org.javarosa.clforms.util.SimpleOrderedHashtable;
import org.javarosa.properties.PropertyManager;

import de.enough.polish.ui.ChoiceGroup;
import de.enough.polish.ui.Container;
import de.enough.polish.ui.FramedForm;
import de.enough.polish.ui.Item;
import de.enough.polish.ui.ItemStateListener;
import de.enough.polish.ui.StringItem;
import de.enough.polish.ui.TextField;

public class PropertiesScreen extends FramedForm implements ItemStateListener {

    public static Command CMD_DONE = new Command("Done", Command.BACK, 1);

    public static Command CMD_CANCEL = new Command("Cancel", Command.BACK, 1);

    SimpleOrderedHashtable itemChoices = new SimpleOrderedHashtable();

    SimpleOrderedHashtable changes = new SimpleOrderedHashtable();

    Display currentDisplay;

    Displayable currentScreen;

    public PropertiesScreen() {
        super("Properties");
        populateProperties();
        addRMSInfo();
        this.addCommand(CMD_DONE);
        this.addCommand(CMD_CANCEL);

        this.setItemStateListener(this);
    }

    public void showPropertiesScreen(Display currentDisplay,
            Displayable currentScreen) {
        this.currentDisplay = currentDisplay;
        this.currentScreen = currentScreen;
        currentDisplay.setCurrent(this);
    }

    private void populateProperties() {
        if (PropertyManager.instance().getRules() != null) {
            Vector properties = PropertyManager.instance().getRules()
                    .allowableProperties();

            for (int i = 0; i < properties.size(); ++i) {
                String propertyName = (String) properties.elementAt(i);
                Vector options = PropertyManager.instance().getRules()
                        .allowableValues(propertyName);
                // Check to see whether this property has a dynamic rules list
                if (options.size() == 1) {
                    String option = (String) options.elementAt(0);
                    if (PropertyManager.instance().getRules()
                            .checkPropertyAllowed(option)) {
                        // this is a Dynamic property list, replace options
                        options = PropertyManager.instance()
                                .getProperty(option);
                    }
                }
                // If there are no options, it is an internal system's variable.
                // Don't touch
                if (options.size() != 0) {
                    Vector propValues = PropertyManager.instance().getProperty(
                            propertyName);
                    // We can easily add the functionality to use multiple
                    // possible choices here but
                    // for now, we'll stick with single-selection properties
                    if (propValues.size() == 1) {
                        // Pull the property's current value
                        String currentSelection = (String) propValues
                                .elementAt(0);

                        // Create the UI Elements
                        ChoiceGroup newChoiceGroup = new ChoiceGroup(
                                propertyName, ChoiceGroup.EXCLUSIVE);
                        itemChoices.put(newChoiceGroup, options);

                        // Seek through to find the property in the list of
                        // potentials
                        int selindex = 0;
                        for (int j = 0; j < options.size(); ++j) {
                            String option = (String) options.elementAt(j);
                            if (option.equals(currentSelection)) {
                                selindex = j;
                            }
                            newChoiceGroup.append(option, null);
                        }

                        // Finish it all up
                        newChoiceGroup.setSelectedIndex(selindex, true);
                        this.append(newChoiceGroup);
                    }
                } else {
                    Vector propValues = PropertyManager.instance().getProperty(
                            propertyName);
                    // We can easily add the functionality to use multiple
                    // possible choices here but
                    // for now, we'll stick with single-selection properties
                    if (propValues.size() <= 1
                            && !PropertyManager.instance().getRules()
                                    .checkPropertyUserReadOnly(propertyName)) {
                        TextField input = new TextField(propertyName,
                                (String) propValues.elementAt(0), 50,
                                TextField.ANY);
                        this.append(input);

                    }
                }
            }
        }
    }

    private void addRMSInfo() {
        // #style textBox
        Container c = new Container(false);
        c.setLabel("Storage Info");

        String[] recordStores = RecordStore.listRecordStores();
        int consumedSpace[] = new int[recordStores.length];
        int availableSpace = -1;

        for (int i = 0; i < recordStores.length; i++) {
            try {
                RecordStore r = RecordStore.openRecordStore(recordStores[i],
                        false);
                consumedSpace[i] = r.getSize();
                if (availableSpace == -1)
                    availableSpace = r.getSizeAvailable();
                // really should close the recordstore here, but we don't do it
                // anywhere else and it might introduce bugs
            } catch (RecordStoreException e) {
                consumedSpace[i] = -1;
            }
        }

        c.add(new StringItem(null, "Available Space: "
                + formatBytes(availableSpace)));
        for (int i = 0; i < recordStores.length; i++) {
            c.add(new StringItem(null, recordStores[i] + ": "
                    + formatBytes(consumedSpace[i])));
        }

        append(c);
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

    public void itemStateChanged(Item item) {
        if (item.getClass() == ChoiceGroup.class) {
            ChoiceGroup cg = (ChoiceGroup) item;
            Vector choices = (Vector) itemChoices.get(cg);
            String propertyName = cg.getLabel();
            if (cg.getSelectedIndex() >= 0) {
                System.out.println("Index is " + cg.getSelectedIndex());
                // String selection = cg.get(cg.getSelectedIndex()).getLabel();
                String selection = (String) choices.elementAt(cg
                        .getSelectedIndex());
                // This is a weird way to do this, but essentially works as long
                // as there is only
                // one possible property (which is true for now).
                if (PropertyManager.instance().getProperty(propertyName)
                        .contains(selection)) {
                    changes.remove(propertyName);
                } else {
                    changes.put(propertyName, selection);
                }
            }
        } else if (item.getClass() == TextField.class) {
            TextField tf = (TextField) item;
            String propertyName = tf.getLabel();
            // This is a weird way to do this, but essentially works as long as
            // there is only
            // one possible property (which is true for now).
            if (PropertyManager.instance().getProperty(propertyName).contains(
                    tf.getText())) {
                changes.remove(propertyName);
            } else {
                changes.put(propertyName, tf.getText());
            }
        }
    }

    public void commitChanges() {
        Enumeration iter = changes.keys();
        while (iter.hasMoreElements()) {
            String propertyName = iter.nextElement().toString();
            String value = changes.get(propertyName).toString();
            PropertyManager.instance().setProperty(propertyName, value);
            System.out.println("Property " + propertyName + " is now " + value);
        }
    }
}

//#endif