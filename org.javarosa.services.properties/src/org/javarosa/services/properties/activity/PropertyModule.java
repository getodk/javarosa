package org.javarosa.services.properties.activity;

import java.util.Enumeration;
import java.util.Hashtable;

import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Displayable;

import org.javarosa.core.Context;
import org.javarosa.core.JavaRosaPlatform;
import org.javarosa.core.api.Constants;
import org.javarosa.core.api.IModule;
import org.javarosa.core.api.IShell;
import org.javarosa.services.properties.view.PropertiesScreen;

public class PropertyModule implements IModule, CommandListener {
	
	private PropertiesScreen screen;
	
    public static Command CMD_DONE = new Command("Done", Command.BACK, 1);

    public static Command CMD_CANCEL = new Command("Cancel", Command.BACK, 1);
    
    private IShell shell;
    
    public PropertyModule(IShell shell) {
    	this.shell = shell;
    }
	
	public void contextChanged(Context globalContext) {
		// Nothing
		
	}

	public void destroy() {
		// Nothing
		
	}

	public void halt() {
		// Nothing
		
	}

	public void resume(Context globalContext) {
		// Nothing
		
	}

	public void start(Context context) {
		 screen = new PropertiesScreen();
		 screen.addCommand(CMD_DONE);
		 screen.addCommand(CMD_CANCEL);
		 screen.setCommandListener(this);
		 shell.setDisplay(this,screen);
	}
	
	public void commandAction(Command command, Displayable displayable) {
		if(command == CMD_DONE) {
			commitChanges(screen.getChanges());
		}
		shell.returnFromModule(this, Constants.ACTIVITY_COMPLETE, null);
	}

    private void commitChanges(Hashtable changes) {
        Enumeration iter = changes.keys();
        while (iter.hasMoreElements()) {
            String propertyName = iter.nextElement().toString();
            String value = changes.get(propertyName).toString();
            JavaRosaPlatform.instance().getPropertyManager().setProperty(propertyName, value);
            System.out.println("Property " + propertyName + " is now " + value);
        }
    }
}
