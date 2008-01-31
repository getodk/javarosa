//#if polish.usePolishGui
package org.javarosa.properties.view;

import java.util.Enumeration;
import java.util.Vector;

import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Display;
import javax.microedition.lcdui.Displayable;

import org.javarosa.clforms.util.SimpleOrderedHashtable;
import org.javarosa.properties.PropertyManager;

import de.enough.polish.ui.ChoiceGroup;
import de.enough.polish.ui.FramedForm;
import de.enough.polish.ui.Item;
import de.enough.polish.ui.ItemStateListener;

public class PropertiesScreen extends FramedForm implements ItemStateListener{

    public static Command CMD_DONE = new Command("Done",Command.BACK,1);
    public static Command CMD_CANCEL = new Command("Cancel",Command.BACK,1);
    
    SimpleOrderedHashtable itemChoices = new SimpleOrderedHashtable();
    
    SimpleOrderedHashtable changes = new SimpleOrderedHashtable();
    
    Display currentDisplay;
    
    Displayable currentScreen;
    
    public PropertiesScreen() {
        super("Properties");
        populateProperties();
        this.addCommand(CMD_DONE);
        this.addCommand(CMD_CANCEL);
        
        this.setItemStateListener(this);
    }
    
    public void showPropertiesScreen(Display currentDisplay, Displayable currentScreen) {
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
                ChoiceGroup newChoiceGroup = new ChoiceGroup(propertyName,
                        ChoiceGroup.EXCLUSIVE);
                Vector options = PropertyManager.instance().getRules()
                        .allowableValues(propertyName);
                itemChoices.put(newChoiceGroup, options);
                String currentSelection = PropertyManager.instance().getProperty(propertyName);
                int selindex = 0;
                for (int j = 0; j < options.size(); ++j) {
                    String option = (String) options.elementAt(j);
                    if(option.equals(currentSelection)) {
                        selindex = j;
                    }
                    newChoiceGroup.append(option, null);
                }
                newChoiceGroup.setSelectedIndex(selindex, true);
                this.append(newChoiceGroup);
            }
        }
    }

    public void itemStateChanged(Item item) {
        ChoiceGroup cg = (ChoiceGroup) item;
        Vector choices = (Vector)itemChoices.get(cg);
        String propertyName = cg.getLabel();
        if (cg.getSelectedIndex() >= 0) {
            System.out.println("Index is "  + cg.getSelectedIndex());
            //String selection = cg.get(cg.getSelectedIndex()).getLabel();
            String selection  = (String)choices.elementAt(cg.getSelectedIndex());
            //String selection = cg.get(cg.getSelectedIndex()).getLabelItem().getLabel();
            if (selection.equals(PropertyManager.instance().getProperty(
                    propertyName))) {
                changes.remove(propertyName);
            } else {
                changes.put(propertyName, selection);
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