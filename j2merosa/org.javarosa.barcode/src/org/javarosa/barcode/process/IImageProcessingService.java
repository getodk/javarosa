package org.javarosa.barcode.process;

import javax.microedition.lcdui.Image;

import org.javarosa.core.model.data.IAnswerData;
import org.javarosa.core.services.IService;

/**
 * @author mel
 * 
 * A service that process and image and returns answer data
 *
 */
public interface IImageProcessingService extends IService {

	public String getName();

	public IAnswerData processImage(Image image)
			throws ImageProcessingException;

}
