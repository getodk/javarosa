package org.javarosa.core.model.utils;

import org.javarosa.core.Context;
import org.javarosa.core.api.Constants;
import org.javarosa.core.model.data.IAnswerData;
import org.javarosa.core.model.data.StringData;

public class ContextPreloadHandler implements IPreloadHandler
{
	public Context context;

	public ContextPreloadHandler()
	{

	}
	public ContextPreloadHandler(Context context)
	{
		this.context = context;
		//initHandler();
	}
	public IAnswerData handle(String preloadParams)
	{
		return preloadContext(preloadParams);
	}

	//The context preload parameter
	private IAnswerData preloadContext(String preloadParams) {
		String value = "";
		String userVal = this.context.getCurrentUser();
		//System.out.println("LOGIN NAME IS "+userVal);
		if ("UserID".equals(preloadParams) && userVal != "")
		{ //retrieve
			value = userVal;
		}

		return new StringData(value);
	}


}