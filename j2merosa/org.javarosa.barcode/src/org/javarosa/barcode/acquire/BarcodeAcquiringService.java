package org.javarosa.barcode.acquire;

import org.javarosa.barcode.process.IBarcodeProcessingService;
import org.javarosa.core.JavaRosaServiceProvider;
import org.javarosa.core.services.UnavailableServiceException;
import org.javarosa.formmanager.view.FormElementBinding;
import org.javarosa.formmanager.view.clforms.acquire.AcquiringQuestionScreen;
import org.javarosa.formmanager.view.clforms.acquire.IAcquiringService;
import org.javarosa.view.clforms.widgets.BarcodeQuestionWidget;

/**
 * @author mel
 * 
 *         A service that handles data acquisition from barcodes. The phone
 *         camera (through mmapi) is used to take a picture of the barcode, and
 *         a BarcodeProcessingService is used to decode it to a string.
 * 
 */
public class BarcodeAcquiringService implements IAcquiringService {

	public String getName() {
		return "Barcode Acquiring Service";
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
		try {
			IBarcodeProcessingService barcodeProcessor = (IBarcodeProcessingService) JavaRosaServiceProvider
					.instance().getService("Barcode Processing Service");
			barcodeWidget.setBarcodeProcessor(barcodeProcessor);
			return barcodeWidget;
		} catch (UnavailableServiceException se) {
			return null;
		}
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

		try {
			IBarcodeProcessingService barcodeProcessor = (IBarcodeProcessingService) JavaRosaServiceProvider
					.instance().getService("Barcode Processing Service");
			barcodeWidget.setBarcodeProcessor(barcodeProcessor);
			return barcodeWidget;
		} catch (UnavailableServiceException se) {
			return null;
		}
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
		try {
			IBarcodeProcessingService barcodeProcessor = (IBarcodeProcessingService) JavaRosaServiceProvider
					.instance().getService("Barcode Processing Service");
			barcodeWidget.setBarcodeProcessor(barcodeProcessor);
			return barcodeWidget;
		} catch (UnavailableServiceException se) {
			return null;
		}
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
		try {
			IBarcodeProcessingService barcodeProcessor = (IBarcodeProcessingService) JavaRosaServiceProvider
					.instance().getService("Barcode Processing Service");
			barcodeWidget.setBarcodeProcessor(barcodeProcessor);
			return barcodeWidget;
		} catch (UnavailableServiceException se) {
			return null;
		}
	}

}
