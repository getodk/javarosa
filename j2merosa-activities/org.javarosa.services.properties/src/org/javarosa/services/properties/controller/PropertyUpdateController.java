/**
 * 
 */
package org.javarosa.services.properties.controller;

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

import javax.microedition.lcdui.ChoiceGroup;
import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.Item;
import javax.microedition.lcdui.ItemStateListener;
import javax.microedition.lcdui.TextField;

import org.javarosa.core.JavaRosaServiceProvider;
import org.javarosa.services.properties.view.PropertiesScreen;

/**
 * @author ctsims
 *
 */
public class PropertyUpdateController implements CommandListener, ItemStateListener{
	
	private PropertiesScreen screen;
	
    private Hashtable changes;

	private PropertyUpdateControllerListener listener;
    
    public static Command CMD_DONE = new Command("Done", Command.BACK, 1);
    public static Command CMD_CANCEL = new Command("Cancel", Command.BACK, 1);


	public PropertyUpdateController(PropertiesScreen screen) {
		this.screen = screen;
		changes = new Hashtable();
		this.screen.setCommandListener(this);
		this.screen.setItemStateListener(this);
		this.screen.addCommand(CMD_DONE);
		this.screen.addCommand(CMD_CANCEL);
	}
	
	public void setListener(PropertyUpdateControllerListener listener) {
		this.listener = listener;
	}

	public void commandAction(Command command, Displayable arg1) {
		if(command == CMD_DONE) {
			commitChanges(changes);
		}
		listener.finished();
	}

    public void itemStateChanged(Item item) {
        if (item instanceof ChoiceGroup) {
            ChoiceGroup cg = (ChoiceGroup) item;
            Vector choices = (Vector) screen.getItemChoices().get(cg);
            String propertyName = screen.nameFromItem(cg);
            if (cg.getSelectedIndex() >= 0) {
                String selection = (String) choices.elementAt(cg
                        .getSelectedIndex());
                // This is a weird way to do this, but essentially works as long
                // as there is only
                // one possible property (which is true for now).
                if (JavaRosaServiceProvider.instance().getPropertyManager().getProperty(propertyName)
                        .contains(selection)) {
                    changes.remove(propertyName);
                } else {
                    changes.put(propertyName, selection);
                }
            }
        } else if (item instanceof TextField) {
            TextField tf = (TextField) item;
            String propertyName = screen.nameFromItem(tf);
            // This is a weird way to do this, but essentially works as long as
            // there is only
            // one possible property (which is true for now).
            Vector prop = JavaRosaServiceProvider.instance().getPropertyManager().getProperty(propertyName);
            if(prop == null) {
            	prop = new Vector();
            }
            if (prop.contains(tf.toString())) {
                changes.remove(propertyName);
            } else {
                changes.put(propertyName, tf.getString());
            }
        }
    }
	/**
	 * Used to write changes from the property screen into the property
	 * manager.
	 * 
	 * @param changes The changes that have been made to existing properties
	 */
    private void commitChanges(Hashtable changes) {
        Enumeration iter = changes.keys();
        while (iter.hasMoreElements()) {
            String propertyName = iter.nextElement().toString();
            String value = changes.get(propertyName).toString();
            JavaRosaServiceProvider.instance().getPropertyManager().setProperty(propertyName, value);
			//#if debug.output==verbose
            System.out.println("Property " + propertyName + " is now " + value);
            //#endif
        }
    }
}
