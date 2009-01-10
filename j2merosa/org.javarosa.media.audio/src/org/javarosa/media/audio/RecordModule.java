package org.javarosa.media.audio;

import org.javarosa.core.Context;
import org.javarosa.core.JavaRosaServiceProvider;
import org.javarosa.core.api.IModule;

public class RecordModule implements IModule 
{
	//@Override
	public void registerModule(Context context) 
	{
		String[] classes = { 
				"org.javarosa.media.audio.model.FileDataPointer",
		};		
		JavaRosaServiceProvider.instance().registerPrototypes(classes);
	}
}
