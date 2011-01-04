//#condition polish.api.locationapi

package org.javarosa.location.service;

import javax.microedition.location.Criteria;
import javax.microedition.location.Location;
import javax.microedition.location.LocationException;
import javax.microedition.location.LocationProvider;
import javax.microedition.location.QualifiedCoordinates;

import org.javarosa.core.api.State;
import org.javarosa.core.services.UnavailableServiceException;
import org.javarosa.j2me.services.DataCaptureService;
import org.javarosa.j2me.services.LocationCaptureService;
import org.javarosa.j2me.services.exception.LocationServiceException;
import org.javarosa.location.activity.LocationCaptureState;

public class J2MELocationService extends LocationCaptureService {

	public static final String serviceName = DataCaptureService.LOCATION;
	public static final int TIMEOUT = 200;

	public J2MELocationService() {
		super();
	}

	private LocationProvider getLocationProvider()
			throws UnavailableServiceException {
		try {
			Criteria c = new Criteria();
			LocationProvider lp = LocationProvider.getInstance(c);
			if (lp != null) {
				return lp;
			} else
				throw new UnavailableServiceException(
						"Location service is unavailable. Unable to start "
								+ serviceName);

		} catch (LocationException le) {
			throw new UnavailableServiceException(
					"Location service is unavailable. Unable to start "
							+ serviceName);
		}

	}

	public State getStateForCapture(LocationReceiver receiver) {
		return new LocationCaptureState(receiver, this);
	}

	public Fix getFix() throws LocationServiceException {

		Fix fix = null;

		try {

			setStatus(LocationCaptureService.WAITING_FOR_FIX);
			LocationProvider lp = getLocationProvider();
			if (lp == null) {
				setStatus(LocationCaptureService.FIX_FAILED);
				throw new LocationServiceException(
						"Fix failed. Null location provider");

			}

			Location loc = lp.getLocation(J2MELocationService.TIMEOUT);
			if (loc == null) {
				setStatus(LocationCaptureService.FIX_FAILED);
				throw new LocationServiceException("Fix failed. Null location");

			}
			QualifiedCoordinates c = loc.getQualifiedCoordinates();
			setStatus(LocationCaptureService.FIX_OBTAINED);
			fix = new Fix(c.getLatitude(), c.getLongitude(), c.getAltitude(), c
					.getHorizontalAccuracy());

		} catch (LocationException le) {
			setStatus(LocationCaptureService.FIX_FAILED);
			throw new LocationServiceException("Fix failed:" + le.getMessage());
		} catch (InterruptedException ie) {
			setStatus(LocationCaptureService.FIX_FAILED);
			throw new LocationServiceException("Fix failed:" + ie.getMessage());
		} catch (UnavailableServiceException ue) {
			setStatus(LocationCaptureService.FIX_FAILED);
			throw new LocationServiceException("Fix failed:" + ue.getMessage());
		}

		return fix;
	}

	public String getType() {
		return serviceName;
	}
}
