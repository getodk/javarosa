/*
 * Copyright (C) 2009 JavaRosa
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package org.javarosa.location.activity;

import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.Displayable;

import org.javarosa.j2me.log.CrashHandler;
import org.javarosa.j2me.log.HandledCommandListener;
import org.javarosa.j2me.log.HandledThread;
import org.javarosa.j2me.services.LocationCaptureService;
import org.javarosa.j2me.services.LocationCaptureService.Fix;
import org.javarosa.j2me.services.LocationCaptureService.LocationStateListener;
import org.javarosa.j2me.services.exception.LocationServiceException;
import org.javarosa.j2me.view.J2MEDisplay;

public class LocationCaptureController implements LocationStateListener,
		HandledCommandListener, Runnable {

	private LocationCaptureTransitions transitions;
	private LocationCaptureService locService;

	private LocationCaptureView locView;
	private Thread captureThread;
	private Fix myLoc;

	public LocationCaptureController(LocationCaptureService lService) {
		this.locService = lService;
		locService.addListener(this);
	}

	public void setTransitions(LocationCaptureTransitions transitions) {
		this.transitions = transitions;
	}

	public void start() {
		locView = new LocationCaptureView();
		locView.setCommandListener(this);
		
		J2MEDisplay.setView(locView);

		captureThread = new HandledThread(this, "CaptureThread");
		captureThread.start();

	}

	public void commandAction(Command c, Displayable d) {
		CrashHandler.commandAction(this, c, d);
	}

	public void _commandAction(Command comm, Displayable disp) {
		if (comm == locView.cancelCommand) {
			transitions.captureCancelled();
		} else if (comm == locView.okCommand) {
			if (locService.getState() == LocationCaptureService.FIX_OBTAINED)
				transitions.captured(myLoc);
			else
				transitions.captureCancelled();
		} else if (comm == locView.retryCommand) {
			locService.reset();
			captureThread = new HandledThread(this, "CaptureThread");
			captureThread.start();
		}
	}

	public void run() {
		try {
			myLoc = locService.getFix();
			//sleep long enough for the message to show
			Thread.sleep(2000);
			transitions.captured(myLoc);
		}

		catch (LocationServiceException le) {
			System.err.println(le.getMessage());
		}
		
		catch (SecurityException se) {
			//no permission
			System.err.println(se.getMessage());
			transitions.captureCancelled();
		}
		
		catch (InterruptedException ie) {
			//user cancelled
			locService.reset();
		}

	}

	public void onChange(int state) {
		if (locView != null)
			locView.resetView(state);
	}

}
