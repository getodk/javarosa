package org.javarosa.j2me.services.receiver;

import org.javarosa.j2me.services.LocationCaptureService;
import org.javarosa.j2me.services.LocationCaptureService.Fix;

/**
 * Implemented by states that can receive a location fix
 * 
 * @author melissa
 * 
 */
public interface LocationReceiver {

	/**
	 * A state wanting to receive a location has to provide a
	 * LocationCaptureService. If none is available, the calling state should
	 * handle this
	 * 
	 * @return a LocationCaptureService
	 */
	LocationCaptureService getLocationService();

	/**
	 * Callback; fix has been obtained
	 * 
	 * @param fix
	 */
	void fixObtained(Fix fix);

	/**
	 * Transition back to the calling state; fix has not been obtained
	 */
	void fixFailed();
}
