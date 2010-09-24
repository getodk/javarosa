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

package org.javarosa.j2me.services;

import java.util.Vector;

import org.javarosa.core.api.State;
import org.javarosa.j2me.services.exception.LocationServiceException;

/**
 * 
 * A service to get a location fix
 * 
 * @author mel
 * 
 */
public abstract class LocationCaptureService implements DataCaptureService {

	public static final int NOT_INITIALISED = 0;
	public static final int READY = 1;
	public static final int WAITING_FOR_FIX = 2;
	public static final int FIX_OBTAINED = 3;
	public static final int FIX_FAILED = 4;

	private int status = LocationCaptureService.NOT_INITIALISED;

	private Vector listeners = new Vector();

	/**
	 * @return current service status
	 */
	public int getStatus() {
		return status;
	}

	/**
	 * @param newState
	 *            the service status to set
	 */
	protected void setStatus(int newState) {
		status = newState;
		notifyStateChanged();
	}

	/**
	 * @param receiver
	 *            the state that will receive the results of the location
	 *            capture attempt
	 * @return get a State that uses this service to perform location capture
	 */
	public abstract State getStateForCapture(LocationReceiver receiver);

	/**
	 * @param listener
	 *            a LocationStateListener to notify of changes to the service
	 *            state
	 */
	public void addListener(LocationStateListener listener) {
		listeners.addElement(listener);
	}

	/**
	 * Notify all listeners that the state of the service has changed
	 */
	protected void notifyStateChanged() {
		for (int i = 0; i < listeners.size(); i++) {
			((LocationStateListener) this.listeners.elementAt(i))
					.onChange(getStatus());
		}
	}

	/**
	 * @return a location fix
	 * @throws LocationServiceException
	 */
	public abstract Fix getFix() throws LocationServiceException;

	/**
	 * @author mel a location fix
	 * 
	 */
	public class Fix {
		private double lat;
		private double lon;
		private double altitude;
		private double accuracy;

		public Fix(double lat, double lon, double altitude, double accuracy) {
			super();
			this.lat = lat;
			this.lon = lon;
			this.altitude = altitude;
			this.accuracy = accuracy;
		}

		public double getLat() {
			return lat;
		}

		public double getLon() {
			return lon;
		}

		public double getAltitude() {
			return altitude;
		}

		public double getAccuracy() {
			return accuracy;
		}

	}

	/**
	 * @author mel implementers of this interface want to be notified when the
	 *         state of the location service changes
	 * 
	 */
	public interface LocationStateListener {

		public void onChange(int status);

	}
	
	/**
	 * Implemented by states that can receive a location fix
	 * 
	 * @author melissa
	 * 
	 */
	public interface LocationReceiver {

		/**
		 * Transition back to the calling state; fix has been obtained
		 * 
		 * @param fix
		 */
		void fixObtained(Fix fix);

		/**
		 * Transition back to the calling state; fix has not been obtained
		 */
		void fixFailed();
	}

}
