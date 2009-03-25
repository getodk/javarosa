package org.javarosa.barcode.process;

import javax.microedition.lcdui.Image;

import org.javarosa.core.model.data.IAnswerData;

/**
 * @author mel
 * 
 * A service that decodes barcode data from images.
 *
 */
public interface IBarcodeProcessingService extends IImageProcessingService {

	public String getName();

	public IAnswerData processImage(Image image)
			throws ImageProcessingException;

}
