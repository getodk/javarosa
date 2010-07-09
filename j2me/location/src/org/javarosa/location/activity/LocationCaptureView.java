package org.javarosa.location.activity;

import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.Form;
import javax.microedition.lcdui.Gauge;
import javax.microedition.lcdui.StringItem;

import org.javarosa.j2me.services.LocationCaptureService;

public class LocationCaptureView extends Form {
	
	public Command cancelCommand;
	public Command okCommand;
	public Command retryCommand;
	
	//#style loadingGauge
	private Gauge busyIndicator = new Gauge( null, false, Gauge.INDEFINITE, Gauge.CONTINUOUS_IDLE );


	public LocationCaptureView() {
		//#style capturingPopup
		super("Capture Location");
		append(busyIndicator);
		initCommands();
	}

	private void initCommands() {
		cancelCommand = new Command("Cancel", Command.CANCEL, 0);
		okCommand = new Command("Ok", Command.OK, 0);
		retryCommand = new Command("Retry", Command.SCREEN, 0);
	}

	public void resetView(int status) {

		if (status == LocationCaptureService.WAITING_FOR_FIX) {
			busyIndicator.setLabel("Waiting for fix..");
			busyIndicator.setValue(Gauge.CONTINUOUS_RUNNING);
			this.addCommand(cancelCommand);
			this.removeCommand(okCommand);
			this.removeCommand(retryCommand);
		} else if (status == LocationCaptureService.FIX_OBTAINED) {
			busyIndicator.setLabel("Fix obtained!");
			busyIndicator.setValue(Gauge.CONTINUOUS_IDLE);
			this.addCommand(okCommand);
			this.removeCommand(cancelCommand);
			this.removeCommand(retryCommand);
		} else if (status == LocationCaptureService.FIX_FAILED) {
			busyIndicator.setLabel("Location capture failed.");
			busyIndicator.setValue(Gauge.CONTINUOUS_IDLE);
			this.addCommand(cancelCommand);
			this.removeCommand(okCommand);
			this.addCommand(retryCommand);
		}
		else if (status == LocationCaptureService.READY) {
			busyIndicator.setLabel("Location capture starting...");
			busyIndicator.setValue(Gauge.CONTINUOUS_IDLE);
			this.addCommand(cancelCommand);
			this.removeCommand(okCommand);
			this.removeCommand(retryCommand);
		}
	}

}