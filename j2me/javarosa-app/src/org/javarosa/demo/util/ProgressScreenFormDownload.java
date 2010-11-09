package org.javarosa.demo.util;

import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Gauge;

import org.javarosa.formmanager.view.ProgressScreen;

import de.enough.polish.ui.UiAccess;


public class ProgressScreenFormDownload extends ProgressScreen {

	public ProgressScreenFormDownload(String title, String msg, CommandListener cmdListener) {
		super(title, msg, cmdListener, true);
		
		//#style capturingPopup
		UiAccess.setStyle(this);
		//#style capturingGauge
		UiAccess.setStyle(progressbar);
	}
	
	public void stopProgressBar(){
		progressbar.setValue(Gauge.CONTINUOUS_IDLE);
	}
	
	public void startProgressBar(){
		progressbar.setValue(Gauge.CONTINUOUS_RUNNING);
	}
	
}
	