package org.javarosa.services.properties.activity;

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

import org.javarosa.core.Context;
import org.javarosa.core.JavaRosaServiceProvider;
import org.javarosa.core.api.Constants;
import org.javarosa.core.api.IActivity;
import org.javarosa.core.api.IShell;
import org.javarosa.services.properties.view.PropertiesScreen;

/**
 * The property screen activity is responsible for launching a screen
 * which displays the current property options that are registered with
 * the PropertyManager's rulessets, and allowing the user to update
 * properties within the specifications of those rules.
 * 
 * @author Clayton Sims
 *
 */
public class PropertyScreenActivity implements IActivity, CommandListener, ItemStateListener {
	
	private PropertiesScreen screen;
	
    public static Command CMD_DONE = new Command("Done", Command.BACK, 1);

    public static Command CMD_CANCEL = new Command("Cancel", Command.BACK, 1);
    
    private IShell shell;
    
    private Hashtable changes;
    
    /**
     * Creates a new PropertyScreenActivity for the given shell
     * 
     * @param shell The shell that is controlling the current workflow
     */
    public PropertyScreenActivity(IShell shell) {
    	this.shell = shell;
    }
	
    /*
     * (non-Javadoc)
     * @see org.javarosa.core.api.IActivity#contextChanged(org.javarosa.core.Context)
     */
	public void contextChanged(Context globalContext) {
		// Nothing
		
	}

	/*
	 * (non-Javadoc)
	 * @see org.javarosa.core.api.IActivity#destroy()
	 */
	public void destroy() {
		// Nothing
		
	}

	/*
	 * (non-Javadoc)
	 * @see org.javarosa.core.api.IActivity#halt()
	 */
	public void halt() {
		// Nothing
		
	}

	/*
	 * (non-Javadoc)
	 * @see org.javarosa.core.api.IActivity#resume(org.javarosa.core.Context)
	 */
	public void resume(Context globalContext) {
		// Nothing
		
	}

	/*
	 * (non-Javadoc)
	 * @see org.javarosa.core.api.IActivity#start(org.javarosa.core.Context)
	 */
	public void start(Context context) {
    	changes = new Hashtable();
		 screen = new PropertiesScreen(JavaRosaServiceProvider.instance().getPropertyManager());
		 screen.addCommand(CMD_DONE);
		 screen.addCommand(CMD_CANCEL);
		 screen.setCommandListener(this);
		 screen.setItemStateListener(this);
		 shell.setDisplay(this,screen);
	}
	
	/*
	 * (non-Javadoc)
	 * @see javax.microedition.lcdui.CommandListener#commandAction(javax.microedition.lcdui.Command, javax.microedition.lcdui.Displayable)
	 */
	public void commandAction(Command command, Displayable displayable) {
		if(command == CMD_DONE) {
			commitChanges(changes);
		}
		shell.returnFromActivity(this, Constants.ACTIVITY_COMPLETE, null);
	}
	

    public void itemStateChanged(Item item) {
        if (item instanceof ChoiceGroup) {
            ChoiceGroup cg = (ChoiceGroup) item;
            Vector choices = (Vector) screen.getItemChoices().get(cg);
            String propertyName = cg.getLabel();
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
            String propertyName = tf.getLabel();
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
