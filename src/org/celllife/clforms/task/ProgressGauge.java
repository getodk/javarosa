package org.celllife.clforms.task;

import javax.microedition.lcdui.Alert;
import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Display;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.Form;
import javax.microedition.lcdui.Gauge;

public class ProgressGauge extends Form implements Runnable, CommandListener {

	private boolean stopped; // whether this is stopped

	private Gauge g;

	private Thread th; // Gauge thread.

	private BackgroundTask bktk; // The worker thread.

	private Displayable prevScreen; // The screen to go to if the Gauge is 

	// failed or stopped mannually.
	private Displayable nextScreen = null;

	private Alert alertScreen = null;

	private Display display;

	private static final Command cancelCmd = new Command("Cancel",
			Command.BACK, 2);

	public ProgressGauge(BackgroundTask b, String title, Display d,
			Displayable p) {
		super("Please wait...");
		prevScreen = p;
		bktk = b; // the worker (background) thread
		init(title, d);
	}

	private void init(String title, Display d) {
		try {
			display = d;
			th = new Thread(this);
			th.setPriority(Thread.MIN_PRIORITY);

			g = new Gauge(title, false, Gauge.INDEFINITE,
					Gauge.INCREMENTAL_IDLE);
			append(g);
			addCommand(cancelCmd);
			setCommandListener(this);

			start();
		} catch (Exception e) {
			System.out.println("Error starting the Gauge");
			display.setCurrent(prevScreen);
		}
	}

	public void start() {
		stopped = false;
		// only start the tread if not alive
		if (th.isAlive() == false) {
			th.start();
		}
	}

	public void stop() {
		if (nextScreen == null) {
			nextScreen = prevScreen;
		}
		stopped = true;
	}

	public void run() {

		// In rare cases, stop() might be called after the thread
		// start() called but run() is not yet called ...
		if (!stopped)
			display.setCurrent(this);

		// do until notified to stop/quit
		while (stopped == false) {
			g.setValue(Gauge.INCREMENTAL_UPDATING);
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		g = null;

		if (alertScreen == null) {
			display.setCurrent(nextScreen);
		} else {
			display.setCurrent(alertScreen, nextScreen);
		}
	}

	public void setNextScreen(Alert a, Displayable d) {
		alertScreen = a;
		nextScreen = d;
	}

	public void setNextScreen(Displayable d) {
		alertScreen = null;
		nextScreen = d;
	}

	public void commandAction(Command c, Displayable d) {
		if (c == cancelCmd) {
			stop();
			bktk.stop();
			display.setCurrent(prevScreen);
		}
	}

}
