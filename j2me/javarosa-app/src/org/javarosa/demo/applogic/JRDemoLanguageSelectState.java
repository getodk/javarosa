package org.javarosa.demo.applogic;

import org.javarosa.core.api.State;
import org.javarosa.core.services.PropertyManager;
import org.javarosa.core.services.locale.Localization;
import org.javarosa.core.services.properties.JavaRosaPropertyRules;
import org.javarosa.core.util.PropertyUtils;
import org.javarosa.demo.activity.selectlanguage.JRDemoSelectLanguageController;
import org.javarosa.demo.activity.selectlanguage.JRDemoSelectLanguageTransitions;
import org.javarosa.resources.locale.LanguageUtils;


public class JRDemoLanguageSelectState implements JRDemoSelectLanguageTransitions, State {

	public void start() {
		JRDemoSelectLanguageController ctrl = new JRDemoSelectLanguageController();
		ctrl.setTransitions(this);
		ctrl.start();	
	}
	
	
	public void exit ()
	{
	}
	
	public void languageSelected(String language)
	{
		System.out.println("Before Selection :"+ Localization.get("jrdemo.selectlanguage.title"));

		System.out.println("The language selected was : "+language);

		
		Localization.setLocale(language);
		
		
		System.out.println("IS passed of from initialization : ");
	
		System.out.println("After Selection :"+ Localization.get("jrdemo.selectlanguage.title"));
	
		new JRDemoSplashScreenState().start();
		
		//new JRDemoFormListState().start();

	}
}
