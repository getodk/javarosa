/**
 * 
 */
package org.javarosa.polish.activity.loadingscreen;

import java.util.Timer;
import java.util.TimerTask;

import javax.microedition.lcdui.Form;
import javax.microedition.lcdui.Gauge;

import org.javarosa.core.JavaRosaServiceProvider;
import org.javarosa.core.api.IView;

/**
 * @author Clayton Sims
 * @date Jan 21, 2009
 * 
 */
public class LoadingScreen extends Form implements IView {

	private Timer timer;

	public final static String LOADING_STR = JavaRosaServiceProvider.instance().localize("view.loading");

	public LoadingScreen() {
		super(LOADING_STR);

		// Setup the screen
		final Gauge g = new Gauge(LOADING_STR, false, Gauge.INDEFINITE,
				Gauge.INCREMENTAL_IDLE);
		append(g);
		
		TimerTask r = new TimerTask() {
			public void run() {
				g.setValue(Gauge.INCREMENTAL_UPDATING);
			}

		};
		this.timer = new Timer();
		this.timer.schedule(r, 0, 500);
	}

	public void destroy() {
		this.timer.cancel();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.javarosa.core.api.IView#getScreenObject()
	 */
	public Object getScreenObject() {
		return this;
	}

}
