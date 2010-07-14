package org.javarosa.location.activity;

import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.Form;
import javax.microedition.lcdui.Gauge;

import org.javarosa.core.services.locale.Localization;
import org.javarosa.j2me.services.LocationCaptureService;



public class LocationCaptureView extends Form {
	
	public Command cancelCommand;
	public Command okCommand;
	public Command retryCommand;
	
	//#style capturingGauge
	private Gauge busyIndicator = new Gauge(Localization
			.get("activity.locationcapture.waitingforfix"), false, Gauge.INDEFINITE, Gauge.CONTINUOUS_IDLE );


	public LocationCaptureView() {
		//#style capturingPopup
		super(Localization.get("activity.locationcapture.capturelocation"));
		append(busyIndicator);
		initCommands();
	}

	private void initCommands() {
		cancelCommand = new Command(Localization.get("polish.command.cancel"),
				Command.CANCEL, 0);
		okCommand = new Command(Localization.get("polish.command.ok"), Command.OK, 0);
		retryCommand = new Command(Localization.get("menu.retry"), Command.OK, 0);
	}

	public void resetView(int status) {

		if (status == LocationCaptureService.WAITING_FOR_FIX) {
			busyIndicator.setLabel(Localization
					.get("activity.locationcapture.waitingforfix"));
			busyIndicator.setValue(Gauge.CONTINUOUS_RUNNING);
			this.addCommand(cancelCommand);
			this.removeCommand(okCommand);
			this.removeCommand(retryCommand);
		} else if (status == LocationCaptureService.FIX_OBTAINED) {
			busyIndicator.setLabel(Localization
					.get("activity.locationcapture.fixobtained"));
			busyIndicator.setValue(Gauge.CONTINUOUS_IDLE);
			this.addCommand(okCommand);
			this.removeCommand(cancelCommand);
			this.removeCommand(retryCommand);
		} else if (status == LocationCaptureService.FIX_FAILED) {
			busyIndicator.setLabel(Localization
					.get("activity.locationcapture.fixfailed"));
			busyIndicator.setValue(Gauge.CONTINUOUS_IDLE);
			this.addCommand(cancelCommand);
			this.removeCommand(okCommand);
			this.addCommand(retryCommand);
		} else if (status == LocationCaptureService.READY) {
			busyIndicator.setLabel(Localization
					.get("activity.locationcapture.readyforcapture"));
			busyIndicator.setValue(Gauge.CONTINUOUS_IDLE);
			this.addCommand(cancelCommand);
			this.removeCommand(okCommand);
			this.removeCommand(retryCommand);
		}
	}

}