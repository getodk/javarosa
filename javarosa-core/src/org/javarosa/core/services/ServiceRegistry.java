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

package org.javarosa.core.services;

import java.util.Hashtable;

/**
 * This is a global registry of services.
 * 
 * A 'service' is an interface implementation that provides data from somewhere outside JavaRosa, typically
 * through a vendor-specific API not available on all devices.
 * 
 * To prevent shared code from crashing/not compiling on a device that lacks the device specific API, the
 * shared code must instead communicate only with an abstract API defined in JavaRosa (i.e.,
 * 'VideoCaptureService'). The deployment then provides an implementation of this service that acts as
 * an intermediary, and communicates directly with the device-specific API.
 * 
 * In order for the shared code to access this device-specific implementation, it fetches it from this class
 * -- the registry. The deployment registers its implementation of the service when it initializes.
 * 
 * @author Drew Roos
 *
 */
public class ServiceRegistry {
	public static final String FILE_IO_SVC = "file";
	public static final String IMAGE_SVC = "image";
	public static final String AUDIO_SVC = "audio";
	public static final String VIDEO_SVC = "video";
	public static final String BARCODE_SVC = "barcode";
	public static final String LOCATION_SVC = "loc";
	public static final String RFID_SVC = "rfid";
	
	private static Hashtable<String, Object> services = new Hashtable<String, Object>();
	
	public static void registerService (String type, Object service) {
		validateServiceType(type, service);
		services.put(type, service);
	}
	
	public static void unregisterService (String type) {
		if (services.get(type) == null) {
			System.err.println("No service registered for type [" + type + "]");
		} else {
			services.remove(type);
		}
	}
		
	public static Object getService (String type) throws UnavailableServiceException {
		Object service = services.get(type);
		if (service == null) {
			throw new UnavailableServiceException("No service registered for type [" + type + "]");
		} else {
			return service; 
		}
	}
	
	private static void validateServiceType (String type, Object service) {
	  	if ((FILE_IO_SVC.equals(type) && !(service instanceof IFileService)) /*||
	  	    (IMAGE_SVC.equals(type) && !(service instanceof ImageCaptureService)) || 
			(AUDIO_SVC.equals(type) && !(service instanceof AudioCaptureService)) ||
			(VIDEO_SVC.equals(type) && !(service instanceof VideoCaptureService)) ||
			(BARCODE_SVC.equals(type) && !(service instanceof BarcodeCaptureService)) ||
			(LOCATION_SVC.equals(type) && !(service instanceof LocationCaptureService)) ||
			(RFID_SVC.equals(type) && !(service instanceof RFIDCaptureService))*/) {
			throw new RuntimeException("Service is not of the proper type!");
		}
	}

	public static IFileService getFileIOService () throws UnavailableServiceException {
		return (IFileService)getService(FILE_IO_SVC);
	}	
	
	/*
	public static ImageCaptureService getImageCaptureService () throws UnavailableServiceException {
		return (ImageCaptureService)getService(IMAGE_SVC);
	}

	public static AudioCaptureService getAudioCaptureService () throws UnavailableServiceException {
		return (AudioCaptureService)getService(AUDIO_SVC);
	}

	public static VideoCaptureService getVideoCaptureService () throws UnavailableServiceException {
		return (VideoCaptureService)getService(VIDEO_SVC);
	}

	public static BarcodeCaptureService getBarcodeCaptureService () throws UnavailableServiceException {
		return (BarcodeCaptureService)getService(BARCODE_SVC);
	}

	public static LocationCaptureService getLocationCaptureService () throws UnavailableServiceException {
		return (LocationCaptureService)getService(LOCATION_SVC);
	}

	public static RFIDCaptureService getRFIDCaptureService () throws UnavailableServiceException {
		return (RFIDCaptureService)getService(RFID_SVC);
	}
	 */
	
}
