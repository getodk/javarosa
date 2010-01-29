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

import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Image;
import javax.microedition.media.MediaException;

import org.javarosa.core.model.data.IAnswerData;
import org.javarosa.core.model.data.StringData;
import org.javarosa.formmanager.view.singlequestionscreen.acquire.AcquiringQuestionScreen;
import org.javarosa.j2me.services.BarcodeCaptureService;
import org.javarosa.j2me.services.exception.ImageProcessingException;

/**
 * @author mel
 * 
 *         Barcode image capture (using MMAPI) and processing (using any
 *         available BarcodeProcessingService)
 * 
 */
public class BarcodeCaptureScreen extends ImageCaptureScreen {

	private StringData scannedCode;
	private BarcodeCaptureService barcodeProcessor;

	/**
	 * @return the barcode processing service being used
	 */
	public BarcodeCaptureService getBarcodeProcessor() {
		return barcodeProcessor;
	}

	/**
	 * @param barcodeProcessor
	 *            the barcode processing service to use
	 */
	public void setBarcodeProcessor(BarcodeCaptureService barcodeProcessor) {
		this.barcodeProcessor = barcodeProcessor;
	}

	/**
	 * @param title
	 *            The screen title to use
	 * @param questionScreen
	 *            The calling question screen (to which the acquired dat will be
	 *            returned)
	 * @param callingCommandListener
	 *            The calling command listener, to which control will be
	 *            returned when the acquiring process is terminated. This can
	 *            happen when acquiring is cancelled or when data is
	 *            successfully acquired and passed back to the question screen
	 */
	public BarcodeCaptureScreen(String title,
			AcquiringQuestionScreen questionScreen,
			CommandListener callingCommandListener) {
		super(title, questionScreen, callingCommandListener);

	}

	private void processScan() throws MediaException, ImageProcessingException {
		if (barcodeProcessor == null)
			throw new ImageProcessingException("No barcode processor available");

		Image image = getSnapshotImage();
		IAnswerData scanReturn = barcodeProcessor.processImage(image);
		scannedCode = (StringData) scanReturn;

	}

	/* (non-Javadoc)
	 * @see org.javarosa.barcode.acquire.ImageCaptureScreen#getSetCallingScreenDataCommand()
	 */
	protected Command getSetCallingScreenDataCommand() {
		if (takePictureCommand == null)
		{
			//#if javarosa.usepolishlocalisation
			//takePictureCommand = new Command(Locale.get( "menu.Scan"), Command.OK, 3);
			//#else
			takePictureCommand = new Command("Scan", Command.OK, 3);
			//#endif
			
		}
			
		return takePictureCommand;
	}

	protected IAnswerData getAcquiredData() {

		try {
			processScan();
			if (scannedCode != null)
				return scannedCode;
			else {
				System.out.println("Error returning barcode: Scanning failed");
				showMessage("Error returning barcode: Scanning failed", true);
				return null;
			}
		} catch (MediaException me) {
			showMessage("Error returning image: " + me.getMessage(), false);
			return null;
		} catch (ImageProcessingException ie) {
			showMessage(
					"Error returning barcode: " + ie.getMessage() == null ? ""
							: ie.getMessage(), barcodeProcessor == null ? false
							: true);
			return null;
		}

	}

}
