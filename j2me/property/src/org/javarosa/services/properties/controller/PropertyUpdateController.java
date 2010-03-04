/**
 * 
 */
package org.javarosa.services.properties.controller;

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

import javax.microedition.lcdui.ChoiceGroup;
import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.Item;
import javax.microedition.lcdui.TextField;

import org.javarosa.core.services.PropertyManager;
import org.javarosa.core.util.TrivialTransitions;
import org.javarosa.j2me.log.CrashHandler;
import org.javarosa.j2me.log.HandledCommandListener;
import org.javarosa.j2me.log.HandledItemStateListener;
import org.javarosa.j2me.view.J2MEDisplay;
import org.javarosa.services.properties.view.PropertiesScreen;

/**
 * @author ctsims
 *
 */
public class PropertyUpdateController implements HandledCommandListener, HandledItemStateListener{
	
	private PropertiesScreen screen;
	
    private Hashtable changes;

	private TrivialTransitions listener;
    
    public static Command CMD_DONE = new Command("Done", Command.BACK, 1);
    public static Command CMD_CANCEL = new Command("Cancel", Command.BACK, 1);


	public PropertyUpdateController(TrivialTransitions listener) {
		this.screen = new PropertiesScreen(PropertyManager._());
		this.listener = listener;
		changes = new Hashtable();
		this.screen.setCommandListener(this);
		this.screen.setItemStateListener(this);
		this.screen.addCommand(CMD_DONE);
		this.screen.addCommand(CMD_CANCEL);
	}

	public void start() {
		J2MEDisplay.setView(screen);
	}
	
	public void commandAction(Command c, Displayable d) {
		CrashHandler.commandAction(this, c, d);
	}  

	public void _commandAction(Command command, Displayable d) {
		if(command == CMD_DONE) {
			commitChanges(changes);
		}
		listener.done();
	}

	public void itemStateChanged(Item i) {
		CrashHandler.itemStateChanged(this, i);
	}  

	public void _itemStateChanged(Item item) {
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
                if (PropertyManager._().getProperty(propertyName)
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
            Vector prop = PropertyManager._().getProperty(propertyName);
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
            PropertyManager._().setProperty(propertyName, value);
			//#if debug.output==verbose
            System.out.println("Property " + propertyName + " is now " + value);
            //#endif
        }
    }
}
