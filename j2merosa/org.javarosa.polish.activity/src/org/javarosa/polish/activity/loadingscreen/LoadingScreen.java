/**
 * 
 */
package org.javarosa.polish.activity.loadingscreen;

import java.util.Timer;
import java.util.TimerTask;

import javax.microedition.lcdui.Gauge;

import org.javarosa.core.api.IView;

import de.enough.polish.ui.Form;

/**
 * @author Clayton Sims
 * @date Jan 21, 2009 
 *
 */
public class LoadingScreen extends Form implements IView {

	private Gauge g = null;
	Timer timer;
	
	public LoadingScreen() {
		super("Loading");
		
		// Setup the screen
		this.g = new Gauge("Loading", false, Gauge.INDEFINITE,
				Gauge.INCREMENTAL_IDLE);
		append(this.g);
		TimerTask r = new TimerTask() {

			public void run() {
				g.setValue(Gauge.INCREMENTAL_UPDATING);
			}
			
		};
		timer = new Timer();
		timer.schedule(r, 0, 500);
	}
	
	public void destroy(){ 
		timer.cancel();
	}

	
	/* (non-Javadoc)
	 * @see org.javarosa.core.api.IView#getScreenObject()
	 */
	public Object getScreenObject() {
		// TODO Auto-generated method stub
		return this;
	}

}
