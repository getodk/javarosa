/**
 * 
 */
package org.javarosa.j2me.view;

import javax.microedition.lcdui.Form;
import javax.microedition.lcdui.Gauge;

import org.javarosa.core.services.locale.Localization;

/**
 * @author ctsims
 *
 */
public class LoadingScreen extends Form {
	
	private final static int RESOLUTION = 1000;
	
	private Gauge gauge;
	
	public LoadingScreen(boolean interactive) {
		super(Localization.get("loading.screen.title"));
		if(interactive) {
			gauge = new Gauge(Localization.get("loading.screen.message"), true, 0,RESOLUTION);
		} else{
			//#style loadingGauge?
			gauge = new Gauge(Localization.get("loading.screen.message"), false, Gauge.INDEFINITE,Gauge.CONTINUOUS_RUNNING);
		}
		this.append(gauge);
	}

	public void updateProgress(double progress) {
		gauge.setValue((int)Math.floor(RESOLUTION*progress));
	}

}
