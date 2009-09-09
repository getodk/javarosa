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
