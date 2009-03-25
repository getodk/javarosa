package org.javarosa.barcode.process;

import java.util.Hashtable;

import javax.microedition.lcdui.Image;

import org.javarosa.core.model.data.IAnswerData;
import org.javarosa.core.model.data.StringData;

import com.google.zxing.MonochromeBitmapSource;
import com.google.zxing.Reader;
import com.google.zxing.ReaderException;
import com.google.zxing.Result;
import com.google.zxing.client.j2me.LCDUIImageMonochromeBitmapSource;
import com.google.zxing.qrcode.QRCodeReader;

/**
 * @author mel
 * 
 * Use the ZXing library to process a barcode from an image.
 * http://code.google.com/p/zxing/
 *
 */
public class ZXingBarcodeProcessingService implements IBarcodeProcessingService {

	public String getName() {
		return "Barcode Processing Service";
	}

	public IAnswerData processImage(Image image)
			throws ImageProcessingException {
		MonochromeBitmapSource source = new LCDUIImageMonochromeBitmapSource(
				image);
		Reader reader = new QRCodeReader();
		Hashtable hints = new Hashtable();
		// hints.put(DecodeHintType.TRY_HARDER, Boolean.TRUE);

		try {
			Result result = reader.decode(source, hints);
			if ((result != null) && (result.getText() != null)) {
				String scannedCode = result.getText();
				return new StringData(scannedCode);
			} else {
				throw new ImageProcessingException("Barcode scanning failed");
			}
		} catch (ReaderException re) {
			throw new ImageProcessingException("Barcode scanning failed");
		}
	}
}
