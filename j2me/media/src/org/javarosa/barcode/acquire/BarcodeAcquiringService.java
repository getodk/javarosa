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

package org.javarosa.barcode.acquire;

import org.javarosa.core.services.UnavailableServiceException;
import org.javarosa.formmanager.view.FormElementBinding;
import org.javarosa.formmanager.view.clforms.acquire.AcquiringQuestionScreen;
import org.javarosa.formmanager.view.clforms.acquire.IAcquiringService;
import org.javarosa.j2me.services.BarcodeCaptureService;
import org.javarosa.j2me.services.DataCaptureService;
import org.javarosa.j2me.services.DataCaptureServiceRegistry;
import org.javarosa.view.clforms.widgets.BarcodeQuestionWidget;

/**
 * @author mel
 * 
 *         A service that handles data acquisition from barcodes. The phone
 *         camera (through mmapi) is used to take a picture of the barcode, and
 *         a BarcodeProcessingService is used to decode it to a string.
 * 
 */
//not a real data capture service... seems too tied to the clforms api
public class BarcodeAcquiringService implements DataCaptureService, IAcquiringService {

	BarcodeCaptureService barcoder;
	
	public BarcodeAcquiringService (DataCaptureServiceRegistry dc) {
		try {
			barcoder = dc.getBarcodeCaptureService();
		} catch (UnavailableServiceException e) {
			e.printStackTrace();
		}
	}
	
	public String getType() {
		return "clforms-barcode";
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.javarosa.formmanager.view.clforms.acquire.IAcquiringService#getWidget
	 * (org.javarosa.formmanager.view.FormElementBinding, int)
	 * 
	 * If there is a service that is able to process a barcode from an image,
	 * return a barcode widget to capture the image
	 */
	public AcquiringQuestionScreen getWidget(FormElementBinding prompt, int temp) {
		BarcodeQuestionWidget barcodeWidget = new BarcodeQuestionWidget(prompt,
				temp);
		barcodeWidget.setBarcodeProcessor(barcoder);
		return barcodeWidget;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.javarosa.formmanager.view.clforms.acquire.IAcquiringService#getWidget
	 * (org.javarosa.formmanager.view.FormElementBinding)
	 * 
	 * If there is a service that is able to process a barcode from an image,
	 * return a barcode widget to capture the image
	 */
	public AcquiringQuestionScreen getWidget(FormElementBinding prompt) {
		BarcodeQuestionWidget barcodeWidget = new BarcodeQuestionWidget(prompt);

		barcodeWidget.setBarcodeProcessor(barcoder);
		return barcodeWidget;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.javarosa.formmanager.view.clforms.acquire.IAcquiringService#getWidget
	 * (org.javarosa.formmanager.view.FormElementBinding, java.lang.String)
	 * 
	 * If there is a service that is able to process a barcode from an image,
	 * return a barcode widget to capture the image
	 */
	public AcquiringQuestionScreen getWidget(FormElementBinding prompt,
			String str) {
		BarcodeQuestionWidget barcodeWidget = new BarcodeQuestionWidget(prompt,
				str);
		barcodeWidget.setBarcodeProcessor(barcoder);
		return barcodeWidget;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.javarosa.formmanager.view.clforms.acquire.IAcquiringService#getWidget
	 * (org.javarosa.formmanager.view.FormElementBinding, char)
	 * 
	 * If there is a service that is able to process a barcode from an image,
	 * return a barcode widget to capture the image
	 */
	public AcquiringQuestionScreen getWidget(FormElementBinding prompt, char str) {
		BarcodeQuestionWidget barcodeWidget = new BarcodeQuestionWidget(prompt,
				str);
		barcodeWidget.setBarcodeProcessor(barcoder);
		return barcodeWidget;
	}

}
