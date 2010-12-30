package org.javarosa.demo.applogic;

import org.javarosa.core.services.locale.Localization;
import org.javarosa.splashscreen.api.SplashScreenState;
import org.javarosa.user.utility.UserUtility;

public class JRDemoSplashScreenState extends SplashScreenState {

	public JRDemoSplashScreenState() {
		super(Localization.get("splashscreen"));
	}

	public void done() {
		
//		new JRDemoLanguageSelectState().start();

		//#if javarosa.dev.shortcuts
		new JRDemoLoginState().loggedIn(UserUtility.demoUser(true));
		//#else
		new JRDemoLoginState().start();
		//#endif
	}

}
