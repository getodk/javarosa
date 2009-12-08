/**
 * 
 */
package org.javarosa.j2me.view;

import javax.microedition.lcdui.Form;
import javax.microedition.lcdui.Gauge;

/**
 * @author ctsims
 *
 */
public class LoadingScreen extends Form {
	
	private final static int RESOLUTION = 1000;
	
	private Gauge gauge;
	
	public LoadingScreen(boolean interactive) {
		super("Loading");
		if(interactive) {
			gauge = new Gauge("Loading...", true, 0,RESOLUTION);
		} else{
			//#style loadingGauge?
			gauge = new Gauge("Loading...", false, Gauge.INDEFINITE,Gauge.CONTINUOUS_RUNNING);
		}
		this.append(gauge);
	}

	public void updateProgress(double progress) {
		gauge.setValue((int)Math.floor(RESOLUTION*progress));
	}

}
