/**
 * 
 */
package org.javarosa.polish.activity.loadingscreen;

import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.Form;
import javax.microedition.lcdui.Gauge;

import org.javarosa.core.api.IView;

/**
 * @author Brian DeRenzi
 *
 */
public class LoadingView extends Form implements IView {
	
	private static final Command cancelCmd = new Command("Cancel",
			Command.BACK, 2);
	
	private Gauge g = null;
	
	public LoadingView() {
		super("Loading");
		
		// Setup the screen
		this.g = new Gauge("Loading", false, Gauge.INDEFINITE,
				Gauge.INCREMENTAL_IDLE);
		append(this.g);
		
		
		// much too complicated right now...
		//addCommand(cancelCmd);
	}
	
	/* (non-Javadoc)
	 * @see org.javarosa.core.api.IView#getScreenObject()
	 */
	public Object getScreenObject() {
		// we're based on a form
		return this;
	}
	
	public void update() {
		this.g.setValue(Gauge.INCREMENTAL_UPDATING);
	}
}
