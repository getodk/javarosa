package org.javarosa.communication.reporting;

import org.javarosa.communication.reporting.properties.FeedbackReportProperties;
import org.javarosa.core.Context;
import org.javarosa.core.JavaRosaServiceProvider;
import org.javarosa.core.api.IModule;

public class FeedbackReportModule implements IModule {

	public void registerModule(Context context) {
		JavaRosaServiceProvider.instance().getPropertyManager().addRules(new FeedbackReportProperties());
	}

}
