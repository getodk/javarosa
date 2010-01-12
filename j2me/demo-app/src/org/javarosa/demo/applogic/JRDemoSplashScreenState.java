package org.javarosa.demo.applogic;

import org.javarosa.core.services.locale.Localization;
import org.javarosa.demo.util.JRDemoUtil;
import org.javarosa.splashscreen.api.SplashScreenState;

public class JRDemoSplashScreenState extends SplashScreenState {

	public JRDemoSplashScreenState() {
		super(Localization.get("splashscreen"));
	}

	public void done() {
		//#if javarosa.dev.shortcuts
		new JRDemoLoginState().loggedIn(JRDemoUtil.demoUser());
		//#else
		new JRDemoLoginState().start();
		//#endif
	}

}
