 package org.javarosa.media.image.utilities;

import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.Image;

public class ImageUtility {

	public static Image createThumbnail(Image image) {
		int sourceWidth = image.getWidth();
		int sourceHeight = image.getHeight();

		int thumbWidth = 64;
		int thumbHeight = -1;

		if (thumbHeight == -1)
			thumbHeight = thumbWidth * sourceHeight / sourceWidth;

		Image thumb = Image.createImage(thumbWidth, thumbHeight);
		Graphics g = thumb.getGraphics();

		for (int y = 0; y < thumbHeight; y++) {
			for (int x = 0; x < thumbWidth; x++) {
				g.setClip(x, y, 1, 1);
				int dx = x * sourceWidth / thumbWidth;
				int dy = y * sourceHeight / thumbHeight;
				g
						.drawImage(image, x - dx, y - dy, Graphics.LEFT
								| Graphics.TOP);
			}
		}

		Image immutableThumb = Image.createImage(thumb);

		return immutableThumb;
	}

	/**
	 * Resizes an image from an input stream.
	 * Hat tip: http://eriksdiary.blogspot.com/2008/03/create-thumbnail-in-j2me.html
	 * This won't work without jsr-234 support which is pretty much non-existant (at least on Nokias)
	 * at this point. 
	 * @param src
	 * @param width
	 * @param height
	 * @return
	 */
	/*
	public static Image resizeImage(InputStream src, int width, int height) {
		MediaProcessor mp;
		try {
			String[] supportedTypes = GlobalManager.getSupportedMediaProcessorInputTypes();
			System.out.println("Got back " + supportedTypes.length + " supported media types");
			for (int i = 0; i < supportedTypes.length; i++) {
				System.out.println("Media type: " + supportedTypes[i]);
				System.out.flush();
			}
			mp = GlobalManager.createMediaProcessor("image/raw");
			System.out.println("Media Processor: " + mp);
			System.out.println("Input Stream: " + src);
			mp.setInput(src, MediaProcessor.UNKNOWN);
			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			mp.setOutput(bos);
			ImageTransformControl fc = (ImageTransformControl) mp
					.getControl("javax.microedition.amms.control.imageeffect.ImageTransformControl");
			fc.setTargetSize(width, height, 0);
			fc.setEnforced(true);
			fc.setEnabled(true);

			mp.complete();
			src.close();
			return Image.createImage(bos.toByteArray(), 0, bos.size());
		} catch (MediaException e) {
			System.out.println("MediaException in ImageUtility.resizeImage():"
					+ e.getMessage());
			e.printStackTrace();
		} catch (IOException e) {
			System.out.println("IOException in ImageUtility.resizeImage():"
					+ e.getMessage());
			e.printStackTrace();
		}
		return null;
	}
	*/
}
