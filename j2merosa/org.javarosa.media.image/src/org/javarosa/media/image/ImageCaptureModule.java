package org.javarosa.media.image;

import org.javarosa.core.Context;
import org.javarosa.core.JavaRosaServiceProvider;
import org.javarosa.core.api.IModule;

public class ImageCaptureModule implements IModule {

	public void registerModule(Context context) {
		String[] classes = {
				"org.javarosa.media.image.model.FileDataPointer",
		};		
		JavaRosaServiceProvider.instance().registerPrototypes(classes);
	}

}
