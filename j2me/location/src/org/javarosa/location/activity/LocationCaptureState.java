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

/**
 * An Activity that represents the capture of audio. 
 * 
 * @author Ndubisi Onuora
 *
 */

package org.javarosa.location.activity;

import org.javarosa.core.api.State;
import org.javarosa.j2me.services.LocationCaptureService;
import org.javarosa.j2me.services.LocationCaptureService.Fix;
import org.javarosa.j2me.services.receiver.LocationReceiver;

public class LocationCaptureState implements LocationCaptureTransitions, State {

	private LocationReceiver receiver;
	private LocationCaptureController controller;

	public LocationCaptureState(LocationReceiver receiver) {
		this.receiver = receiver;
	}

	public void start() {
		LocationCaptureService locationService = receiver.getLocationService();
		if (locationService == null)
			captureCancelled();
		controller = new LocationCaptureController(locationService);
		controller.setTransitions(this);
		controller.start();
	}

	public void captureCancelled() {
		receiver.fixFailed();
	}

	public void captured(Fix fix) {
		receiver.fixObtained(fix);
	}

}