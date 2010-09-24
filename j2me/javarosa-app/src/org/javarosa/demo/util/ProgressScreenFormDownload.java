package org.javarosa.demo.util;

import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Form;
import javax.microedition.lcdui.Gauge;

import org.javarosa.j2me.log.HandledThread;


public class ProgressScreenFormDownload extends Form{
	
	private Gauge progressbar;
	private Thread updater_T;
	public final Command CMD_CANCEL = new Command("Cancel",Command.BACK, 1);
	public final Command CMD_RETRY = new Command("Retry",Command.ITEM, 1);

	public ProgressScreenFormDownload(String title,String msg,CommandListener cmdListener) {
		super(title);
		//#style loadingGauge
		progressbar = new Gauge(null, false, Gauge.INDEFINITE, Gauge.CONTINUOUS_RUNNING );

		addCommand(CMD_CANCEL);
		append(progressbar);
		setCommandListener(cmdListener);
		updater_T = new HandledThread(new GaugeUpdater());
		updater_T.start();
	}
	
	public void setText(String text) {
		progressbar.setLabel(text);
	}
	
	public void closeThread(){
		if(updater_T!=null){
			updater_T = null;			
		}
	}
	
	class GaugeUpdater implements Runnable {
		
		public void run() {

			int i = 0;
			while (progressbar.getValue() < progressbar.getMaxValue()) {
				
				try {
					Thread.sleep(500);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
				//i++;
			//	progressbar.setValue(progressbar.getValue()+(i%2)*2);
			}

		}
	}

	
}
	