package org.javarosa.formmanager.view.clforms;

import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Form;
import javax.microedition.lcdui.Gauge;

public class ProgressScreen extends Form{
	private Gauge progressbar;
	private GaugeUpdater gupd = new GaugeUpdater();
	private Thread updater_T;
	public final Command CMD_CANCEL = new Command("Cancel",Command.BACK, 1);

	public ProgressScreen(String title,String msg,CommandListener cmdListener) {
		super(title);
		progressbar = new Gauge(msg, false, Gauge.INDEFINITE, 0);
		addCommand(CMD_CANCEL);
		append(progressbar);
		setCommandListener(cmdListener);
		updater_T = new Thread(gupd);
		updater_T.start();
	}
	
	public void closeThread(){
		if(updater_T!=null){
			updater_T = null;			
		}
	}
	
	class GaugeUpdater implements Runnable {
		
		public void run() {

			while (progressbar.getValue() < progressbar.getMaxValue()) {
//				progressbar.setValue(progressbar.getValue() + 1);
				progressbar.setValue(Gauge.CONTINUOUS_IDLE);
			}

		}
	}

	
}
