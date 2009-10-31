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

import java.util.Hashtable;

import org.javarosa.core.services.UnavailableServiceException;
import org.javarosa.j2me.services.DataCaptureService;

/**
 * This is a registry of services to be passed to a state (such as form entry) that can perform data capture.
 * 
 * A 'service' is an interface implementation that provides data from somewhere outside JavaRosa, typically
 * through a vendor-specific API not available on all devices.
 * 
 * To prevent shared code from crashing/not compiling on a device that lacks the device specific API, the
 * shared code must instead communicate only with an abstract API defined in JavaRosa (i.e.,
 * 'VideoCaptureService'). The deployment then provides an implementation of this service that acts as
 * an intermediary, and communicates directly with the device-specific API.
 * 
 * @author Drew Roos
 *
 */
public class DataCaptureServiceRegistry {
	
	private Hashtable<String, DataCaptureService> services;
	
	public DataCaptureServiceRegistry () {
		services = new Hashtable<String, DataCaptureService>();
	}
	
	public DataCaptureServiceRegistry (DataCaptureService[] services) {
		this();
		for (int i = 0; i < services.length; i++)
			registerService(services[i]);
	}
	
	public void registerService (DataCaptureService service) {
		String type = service.getName();
		validateServiceType(type, service);
		services.put(type, service);
	}
	
	public void unregisterService (String type) {
		if (services.get(type) == null) {
			System.err.println("No service registered for type [" + type + "]");
		} else {
			services.remove(type);
		}
	}
		
	public DataCaptureService getService (String type) throws UnavailableServiceException {
		DataCaptureService service = services.get(type);
		if (service == null) {
			throw new UnavailableServiceException("No service registered for type [" + type + "]");
		} else {
			return service; 
		}
	}
	
	private static void validateServiceType (String type, DataCaptureService service) {
	  	if (/*
	  		(DataCaptureService.IMAGE.equals(type) && !(service instanceof ImageCaptureService)) || */
			(DataCaptureService.AUDIO.equals(type) && !(service instanceof AudioCaptureService)) /*||
			(DataCaptureService.VIDEO.equals(type) && !(service instanceof VideoCaptureService)) ||
			(DataCaptureService.BARCODE.equals(type) && !(service instanceof BarcodeCaptureService)) ||
			(DataCaptureService.LOCATION.equals(type) && !(service instanceof LocationCaptureService)) ||
			(DataCaptureService.RFID.equals(type) && !(service instanceof RFIDCaptureService))*/) {
			throw new RuntimeException("Service is not of the proper type!");
		}
	}

	/* convenience functions */
	
	/*
	public ImageCaptureService getImageCaptureService () throws UnavailableServiceException {
		return (ImageCaptureService)getService(DataCaptureService.IMAGE);
	}
	*/
		
	public AudioCaptureService getAudioCaptureService () throws UnavailableServiceException {
		return (AudioCaptureService)getService(DataCaptureService.AUDIO);
	}

	/*
	public VideoCaptureService getVideoCaptureService () throws UnavailableServiceException {
		return (VideoCaptureService)getService(DataCaptureService.VIDEO);
	}

	public BarcodeCaptureService getBarcodeCaptureService () throws UnavailableServiceException {
		return (BarcodeCaptureService)getService(DataCaptureService.BARCODE);
	}

	public LocationCaptureService getLocationCaptureService () throws UnavailableServiceException {
		return (LocationCaptureService)getService(DataCaptureService.LOCATION);
	}

	public RFIDCaptureService getRFIDCaptureService () throws UnavailableServiceException {
		return (RFIDCaptureService)getService(DataCaptureService.RFID);
	}
	 */
	
}
