package org.javarosa.core.services.locale;

import java.util.Hashtable;

import org.javarosa.core.reference.ReferenceDataSource;

public class Localization {
	private static Localizer globalLocalizer;
	
	public static String get(String key){
		return get(key, new String[]{});
	}
	
	public static String get(String key, String[] args) {
		checkRep();
		return globalLocalizer.getText(key, args);
	}
	
	public static String get(String key, Hashtable args) {
		checkRep();
		return globalLocalizer.getText(key, args);
	}
	
	public static void registerLanguageFile(String localeName, String resourceFileURI) {
		init();
		if(!globalLocalizer.hasLocale(localeName)){ 
			globalLocalizer.addAvailableLocale(localeName);
		}
		globalLocalizer.registerLocaleResource(localeName, new ResourceFileDataSource(resourceFileURI));
		if(globalLocalizer.getDefaultLocale() == null) {
			globalLocalizer.setDefaultLocale(localeName);
		}
	}
	
	public static void registerLanguageReference(String localeName, String referenceUri) {
		init();
		if(!globalLocalizer.hasLocale(localeName)){ 
			globalLocalizer.addAvailableLocale(localeName);
		}
		globalLocalizer.registerLocaleResource(localeName, new ReferenceDataSource(referenceUri));
		if(globalLocalizer.getDefaultLocale() == null) {
			globalLocalizer.setDefaultLocale(localeName);
		}
	}
	
	public static Localizer getGlobalLocalizerAdvanced() {
		init();
		return globalLocalizer;
	}
	
	public static void setLocale(String locale) {
		checkRep();
		globalLocalizer.setLocale(locale);
	}
	
	public static void setDefaultLocale(String defaultLocale) {
		checkRep();
		globalLocalizer.setDefaultLocale(defaultLocale);
	}
	
	/**
	 * 
	 */
	private static void init() {
		if(globalLocalizer == null) {
			globalLocalizer = new Localizer(true,true);
		}
	}
	/**
	 *  
	 */
	private static void checkRep() {
		init();
		if(globalLocalizer.getAvailableLocales().length == 0) {
			throw new LocaleTextException("There are no locales defined for the application. Please make sure to register locale text using the Locale.register() method");
		}
	}
}
