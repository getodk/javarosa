/**
 * 
 */
package org.javarosa.j2me.view;

import java.util.Timer;

import javax.microedition.lcdui.Display;

import org.javarosa.j2me.log.HandledTimerTask;

/**
 * @author ctsims
 *
 */
public class LoadingScreenThread extends HandledTimerTask {
	private Timer timer;
	private static final long START_THRESHOLD = 500;
	private static final long POLL_PERIOD = 50;
	private long elapsed;
	private boolean displayed;
	private ProgressIndicator indicator;
	private Display display;
	private boolean canceled;
	private LoadingScreen screen;
	
	private Object lock;
	
	
	public LoadingScreenThread(Display d) {
		display = d;
		lock = new Object();
	}
	
	public void startLoading(ProgressIndicator indicator) {
		timer = new Timer();
		screen = new LoadingScreen(indicator != null);
		displayed = false;
		this.indicator = indicator;
		timer.schedule(this, START_THRESHOLD, POLL_PERIOD);
	}

	public void _run() {
		synchronized(lock) {
			if(!canceled) {
				if(!displayed) {
					elapsed += START_THRESHOLD;
					display.setCurrent(screen);
					displayed = true;
				}	
				if(indicator != null) {
					screen.updateProgress(indicator.getProgress());
				}
			}
		}
	}
	
	public void cancelLoading() {
		synchronized(lock) {
			canceled = true;
			displayed= false;
			if(timer != null) {
				timer.cancel();
			}
			indicator = null;
			screen = null;
		}
	}
}
