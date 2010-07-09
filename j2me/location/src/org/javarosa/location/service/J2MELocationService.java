package org.javarosa.location.service;

import javax.microedition.location.Criteria;
import javax.microedition.location.Location;
import javax.microedition.location.LocationException;
import javax.microedition.location.LocationProvider;
import javax.microedition.location.QualifiedCoordinates;

import org.javarosa.core.services.UnavailableServiceException;
import org.javarosa.j2me.services.DataCaptureService;
import org.javarosa.j2me.services.LocationCaptureService;
import org.javarosa.j2me.services.exception.LocationServiceException;

public class J2MELocationService extends LocationCaptureService {

	public static final String serviceName = DataCaptureService.LOCATION;
	public static final int TIMEOUT = 200;
	private LocationProvider lp;

	public J2MELocationService() throws UnavailableServiceException {
		try {
			Criteria c = new Criteria();
			lp = LocationProvider.getInstance(c);

		} catch (LocationException le) {
			throw new UnavailableServiceException(
					"Location service is unavailable. Unable to start "
							+ serviceName);
		}
		
		setState(LocationCaptureService.READY);
	}
	
	public void reset()
	{
		if (lp!=null)
			lp.reset();
		setState(LocationCaptureService.READY);
	}

	public Fix getFix() throws LocationServiceException {

		try {
		
			setState(LocationCaptureService.WAITING_FOR_FIX);
			Location l = lp.getLocation(J2MELocationService.TIMEOUT);
			QualifiedCoordinates c = l.getQualifiedCoordinates();
			setState(LocationCaptureService.FIX_OBTAINED);
			return new Fix(c.getLatitude(), c.getLongitude(), c.getAltitude(),
					c.getHorizontalAccuracy());

		} catch (LocationException le) {
			setState(LocationCaptureService.FIX_FAILED);
		} catch (InterruptedException ie) {
			setState(LocationCaptureService.FIX_FAILED);
		}

		return null;
	}
	
	public String getType() {
		return serviceName;
	}

}
