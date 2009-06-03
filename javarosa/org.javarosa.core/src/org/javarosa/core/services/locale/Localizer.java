/*
 * Copyright (C) 2009 JavaRosa-Core Project
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

package org.javarosa.core.services.locale;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

import org.javarosa.core.util.NoLocalizedTextException;
import org.javarosa.core.util.OrderedHashtable;
import org.javarosa.core.util.UnregisteredLocaleException;
import org.javarosa.core.util.externalizable.DeserializationException;
import org.javarosa.core.util.externalizable.ExtUtil;
import org.javarosa.core.util.externalizable.ExtWrapList;
import org.javarosa.core.util.externalizable.ExtWrapListPoly;
import org.javarosa.core.util.externalizable.ExtWrapMap;
import org.javarosa.core.util.externalizable.ExtWrapMapPoly;
import org.javarosa.core.util.externalizable.ExtWrapNullable;
import org.javarosa.core.util.externalizable.ExtWrapTagged;
import org.javarosa.core.util.externalizable.Externalizable;
import org.javarosa.core.util.externalizable.PrototypeFactory;

/**
 * The Localizer object maintains mappings for locale ID's and Object
 * ID's to the String values associated with them in different 
 * locales.
 * 
 * @author Drew Roos
 *
 */
public class Localizer implements Externalizable {
	private Vector locales; /* Vector<String> */
	private OrderedHashtable localeResources; /* String -> Vector<LocaleDataSource> */
	private OrderedHashtable currentLocaleData; /* Hashtable{ String -> String } */
	private String defaultLocale;
	private String currentLocale;
	private boolean fallbackDefaultLocale;
	private boolean fallbackDefaultForm;
	private Vector observers;
	
	/**
	 * Default constructor. Disables all fallback modes.
	 */
	public Localizer () {
		this(false, false);
	}
	
	/**
	 * Full constructor.
	 * 
	 * @param fallbackDefaultLocale If true, search the default locale when no translation for a particular text handle
	 * is found in the current locale.
	 * @param fallbackDefaultForm If true, search the default text form when no translation is available for the
	 * specified text form ('long', 'short', etc.). Note: form is specified by appending ';[form]' onto the text ID. 
	 */
	public Localizer (boolean fallbackDefaultLocale, boolean fallbackDefaultForm) {
		localeResources = new OrderedHashtable();
		currentLocaleData = new OrderedHashtable(); 
		locales = new Vector();
		defaultLocale = null;
		currentLocale = null;
		observers = new Vector();
		this.fallbackDefaultLocale = fallbackDefaultLocale;
		this.fallbackDefaultForm = fallbackDefaultForm;
	}
	
	public boolean equals (Object o) {
		if (o instanceof Localizer) {
			Localizer l = (Localizer)o;
			
			//TODO: Compare all resources
			return (ExtUtil.equals(locales, locales) &&
					ExtUtil.equals(localeResources, l.localeResources) &&
					ExtUtil.equals(defaultLocale, l.defaultLocale) &&
					ExtUtil.equals(currentLocale, l.currentLocale) &&
					fallbackDefaultLocale == l.fallbackDefaultLocale &&
					fallbackDefaultForm == l.fallbackDefaultForm);
		} else {
			return false;
		}
	}
	
	/**
	 * Get default locale fallback mode
	 * 
	 * @return default locale fallback mode
	 */
	public boolean getFallbackLocale () {
		return fallbackDefaultLocale;
	}
	
	/**
	 * Get default form fallback mode
	 * 
	 * @return default form fallback mode
	 */
	public boolean getFallbackForm () {
		return fallbackDefaultForm;
	}
	
	/* === INFORMATION ABOUT AVAILABLE LOCALES === */
	
	/**
	 * Create a new locale (with no mappings). Do nothing if the locale is already defined.
	 * 
	 * @param locale Locale to add. Must not be null.
	 * @return True if the locale was not already defined.
	 * @throws NullPointerException if locale is null
	 */
	public boolean addAvailableLocale (String locale) {
		if (hasLocale(locale)) {
			return false;
		} else {
			locales.addElement(locale);
			localeResources.put(locale, new Vector());
			return true;
		}
	}
	
	/**
	 * Get a list of defined locales.
	 * 
	 * @return Array of defined locales, in order they were created.
	 */
	public String[] getAvailableLocales () {
		String[] data = new String[locales.size()];
		locales.copyInto(data);
		return data;
	}
	
	/**
	 * Get whether a locale is defined. The locale need not have any mappings.
	 * 
	 * @param locale Locale
	 * @return Whether the locale is defined. False if null
	 */
	public boolean hasLocale (String locale) {
		return (locale == null ? false : locales.contains(locale));
	}
	
	/**
	 * Return the next locale in order, for cycling through locales.
	 * 
	 * @return Next locale following the current locale (if the current locale is the last, cycle back to the beginning).
	 * If the current locale is not set, return the default locale. If the default locale is not set, return null.
	 */
	public String getNextLocale () {
		return currentLocale == null ? defaultLocale
									 : (String)locales.elementAt((locales.indexOf(currentLocale) + 1) % locales.size());
	}
	
	/* === MANAGING CURRENT AND DEFAULT LOCALES === */

	/**
	 * Get the current locale.
	 * 
	 * @return Current locale.
	 */
	public String getLocale () {
		return currentLocale;
	}
	
	/**
	 * Set the current locale. The locale must be defined. Will notify all registered ILocalizables of the change in locale.
	 * 
	 * @param currentLocale Locale. Must be defined and not null.
	 * @throws UnregisteredLocaleException If locale is null or not defined.
	 */
	public void setLocale (String currentLocale) {
		if (!hasLocale(currentLocale))
			throw new UnregisteredLocaleException("Attempted to set to a locale that is not defined");
		
		if (!currentLocale.equals(this.currentLocale)) {
			this.currentLocale = currentLocale;
			loadCurrentLocaleResources();
			alertLocalizables();
		}
	}
	
	/**
	 * Get the default locale.
	 * 
	 * @return Default locale.
	 */
	public String getDefaultLocale () {
		return defaultLocale;
	}
	
	/**
	 * Set the default locale. The locale must be defined.
	 * 
	 * @param defaultLocale Default locale. Must be defined. May be null, in which case there will be no default locale.
	 * @throws UnregisteredLocaleException If locale is not defined.
	 */
	public void setDefaultLocale (String defaultLocale) {
		if (defaultLocale != null && !hasLocale(defaultLocale))
			throw new UnregisteredLocaleException("Attempted to set default to a locale that is not defined");
		
		this.defaultLocale = defaultLocale;
	}
	
	/**
	 * Set the current locale to the default locale. The default locale must be set.
	 * 
	 * @throws IllegalStateException If default locale is not set.
	 */
	public void setToDefault () {
		if (defaultLocale == null)
			throw new IllegalStateException("Attempted to set to default locale when default locale not set");
		
		setLocale(defaultLocale);
	}
	
	/**
	 * Constructs a body of local resources to be the set of Current Locale Data.
	 * 
	 * After loading, the current locale data will contain definitions for each
	 * entry defined by the current locale resources, as well as definitions for any
	 * entry present in the fallback resources but not in those of the current locale.
	 *  
	 * The procedure to accomplish this set is as follows, with overwritting occuring 
	 * when a collision occurs:
	 * 
	 * 1. Load all of the in memory definitions for the default locale if fallback is enabled
	 * 2. For each resource file for the default locale, load each definition if fallback is enabled
	 * 3. Load all of the in memory definitions for the current locale
	 * 4. For each resource file for the current locale, load each definition
	 */
	private void loadCurrentLocaleResources() {
		this.currentLocaleData = getLocaleData(currentLocale);
	}
	
	/**
	 * Moves all relevant entries in the source dictionary into the destination dictionary
	 * @param destination A dictionary of key/value locale pairs that will be modified 
	 * @param source A dictionary of key/value locale pairs that will be copied into 
	 * destination 
	 */
	private void loadTable(OrderedHashtable destination, OrderedHashtable source) {
		for(Enumeration en = source.keys(); en.hasMoreElements(); ) {
			String key = (String)en.nextElement();
			destination.put(key, (String)source.get(key));
		}
	}

	/* === MANAGING LOCALE DATA (TEXT MAPPINGS) === */
	
	/**
	 * Registers a resource file as a source of locale data for the specified
	 * locale.  
	 * 
	 * @param locale The locale of the definitions provided. 
	 * @param resource A LocaleDataSource containing string data for the locale provided
	 * @throws NullPointerException if resource or locale are null
	 */
	public void registerLocaleResource (String locale, LocaleDataSource resource) {
		if(locale == null) {
			throw new NullPointerException("Attempt to register a data source to a null locale in the localizer");
		}
		if(resource == null) {
			throw new NullPointerException("Attempt to register a null data source in the localizer");
		}
		Vector resources = new Vector();
		if(localeResources.contains(locale)) {
			resources = (Vector)localeResources.get(locale);
		}
		resources.addElement(resource);
		localeResources.put(locale, resources);
		
		if(locale.equals(currentLocale)) {
			loadCurrentLocaleResources();
		}
	}
	
	/**
	 * Get the set of mappings for a locale.
	 * 
	 * @param locale Locale
	 * @returns Hashtable representing text mappings for this locale. Returns null if locale not defined or null.
	 */
	public OrderedHashtable getLocaleData (String locale) {
		if(locale == null || !this.locales.contains(locale)) {
			return null;
		}
		OrderedHashtable data = new OrderedHashtable();
		
		// If there's a default locale, we load all of its elements into memory first, then allow
		// the current locale to overwrite any differences between the two.    
		if (fallbackDefaultLocale && defaultLocale != null) {
			Vector defaultResources = (Vector) localeResources.get(defaultLocale);
			for (int i = 0; i < defaultResources.size(); ++i) {
				loadTable(data,((LocaleDataSource)defaultResources.elementAt(i)).getLocalizedText());
			}
		}
		
		Vector resources = (Vector)localeResources.get(locale);
		for(int i = 0 ; i < resources.size() ; ++i ) {
			loadTable(data,((LocaleDataSource)resources.elementAt(i)).getLocalizedText());
		}
		
		return data;
	}

	/**
	 * Get the mappings for a locale, but throw an exception if locale is not defined.
	 * 
	 * @param locale Locale
	 * @return Text mappings for locale.
	 * @throws UnregisteredLocaleException If locale is not defined or null.
	 */
	public OrderedHashtable getLocaleMap (String locale) {
		OrderedHashtable mapping = getLocaleData(locale);
		if (mapping == null)
			throw new UnregisteredLocaleException("Attempted to access an undefined locale.");
		return mapping;
	}
	
	/**
	 * Determine whether a locale has a mapping for a given text handle. Only tests the specified locale and form; does
	 * not fallback to any default locale or text form.
	 * 
	 * @param locale Locale. Must be defined and not null.
	 * @param textID Text handle.
	 * @return True if a mapping exists for the text handle in the given locale.
	 * @throws UnregisteredLocaleException If locale is not defined.
	 */
	public boolean hasMapping (String locale, String textID) {
		if (locale == null || !locales.contains(locale)) {
			throw new UnregisteredLocaleException("Attempted to access an undefined locale (" + locale + ") while checking for a mapping for  " + textID);
		}
		Vector resources = (Vector)localeResources.get(locale);
		for(Enumeration en = resources.elements(); en.hasMoreElements(); ) {
			LocaleDataSource source = (LocaleDataSource)en.nextElement();
			if(source.getLocalizedText().containsKey(textID)) {
				return true;
			}
		}
		return false;
	}
	
	/**
	 * Undefine a locale and remove all its data. Cannot be called on the current locale. If called on the default
	 * locale, no default locale will be set afterward.
	 * 
	 * @param locale Locale to remove. Must not be null. Need not be defined. Must not be the current locale.
	 * @return Whether the locale existed in the first place.
	 * @throws IllegalArgumentException If locale is the current locale.
	 * @throws NullPointerException if locale is null
	 */
	public boolean destroyLocale (String locale) {
		if (locale.equals(currentLocale))
			throw new IllegalArgumentException("Attempted to destroy the current locale");
		
		boolean removed = hasLocale(locale);
		locales.removeElement(locale);
		localeResources.remove(locale);

		if (locale.equals(defaultLocale))
			defaultLocale = null;
		
		return removed;
	}

	/* === RETRIEVING LOCALIZED TEXT === */
	
	/**
	 * Retrieve the localized text for a text handle in the current locale. See getText(String, String) for details.
	 *
	 * @param textID Text handle (text ID appended with optional text form). Must not be null.
	 * @return Localized text. If no text is found after using all fallbacks, return null.
	 * @throws UnregisteredLocaleException If current locale is not set.
	 * @throws NullPointerException if textID is null
	 */
	public String getText (String textID) {
		return getText(textID, currentLocale);
	}
	
	/**
	 * Retrieve the localized text for a text handle in the current locale. See getText(String, String) for details.
	 *
	 * @param textID Text handle (text ID appended with optional text form). Must not be null.
	 * @param args arguments for string variables.
	 * @return Localized text
	 * @throws UnregisteredLocaleException If current locale is not set.
	 * @throws NullPointerException if textID is null
 	 * @throws NoLocalizedTextException If there is no text for the specified id
	 */
	public String getText (String textID, String[] args) {
		String text = getText(textID, currentLocale);
		if(text != null) {
			text = processArguments(text, args);
		} else {
			throw new NoLocalizedTextException("The Localizer could not find a definition for ID: " + textID + " in the '" + currentLocale + "' locale.");
		}
		return text;
	}
	/**
	 * Retrieve the localized text for a text handle in the current locale. See getText(String, String) for details.
	 *
	 * @param textID Text handle (text ID appended with optional text form). Must not be null.
	 * @param args arguments for string variables.
	 * @return Localized text. If no text is found after using all fallbacks, return null.
	 * @throws UnregisteredLocaleException If current locale is not set.
	 * @throws NullPointerException if textID is null
	 * @throws NoLocalizedTextException If there is no text for the specified id
	 */
	public String getText (String textID, Hashtable args) {
		String text = getText(textID, currentLocale);
		if(text != null) {
			text = processArguments(text, args);
		} else {
			throw new NoLocalizedTextException("The Localizer could not find a definition for ID: " + textID + " in the '" + currentLocale + "' locale.");
		}
		return text;
	}
	
	/**
	 * Retrieve localized text for a text handle in the current locale. Like getText(String), however throws exception
	 * if no localized text is found.
	 * 
	 * @param textID Text handle (text ID appended with optional text form). Must not be null.
	 * @return Localized text
	 * @throws NoLocalizedTextException If there is no text for the specified id
	 * @throws UnregisteredLocaleException If current locale is not set
	 * @throws NullPointerException if textID is null
	 */
	public String getLocalizedText (String textID) {
	    String text = getText(textID);
	    if (text == null)
	    	throw new NoLocalizedTextException("Can't find localized text for current locale! text id: [" + textID + "]");
	    return text;
	}
	
	/**
	 * Retrieve the localized text for a text handle in the given locale. If no mapping is found initially, then,
	 * depending on enabled fallback modes, other places will be searched until a mapping is found.
	 * <p>
	 * The search order is thus:
	 * 1) Specified locale, specified text form
	 * 2) Specified locale, default text form
	 * 3) Default locale, specified text form
	 * 4) Default locale, default text form
	 * <p>
	 * (1) and (3) are only searched if a text form ('long', 'short', etc.) is specified.
	 * If a text form is specified, (2) and (4) are only searched if default-form-fallback mode is enabled.
	 * (3) and (4) are only searched if default-locale-fallback mode is enabled. It is not an error in this situation
	 *   if no default locale is set; (3) and (4) will simply not be searched.
	 *
	 * @param textID Text handle (text ID appended with optional text form). Must not be null.
	 * @param locale Locale. Must be defined and not null.
	 * @return Localized text. If no text is found after using all fallbacks, return null.
	 * @throws UnregisteredLocaleException If the locale is not defined or null.
	 * @throws NullPointerException if textID is null
	 */
	public String getText (String textID, String locale) {
		String text = getRawText(locale, textID);
		if (text == null && fallbackDefaultForm && textID.indexOf(";") != -1)
			text = getRawText(locale, textID.substring(0, textID.indexOf(";")));
		if (text == null && fallbackDefaultLocale && !locale.equals(defaultLocale) && defaultLocale != null)
			text = getText(textID, defaultLocale);
		return text;
	}
		
	/**
	 * Get text for locale and exact text ID only, not using any fallbacks.
	 * 
	 * NOTE: This call will only return the full compliment of available strings if and 
	 * only if the requested locale is current. Otherwise it will only retrieve strings
	 * declared at runtime.
	 * 
	 * @param locale Locale. Must be defined and not null.
	 * @param textID Text handle (text ID appended with optional text form). Must not be null.
	 * @return Localized text. Return null if none found.
	 * @throws UnregisteredLocaleException If the locale is not defined or null.
	 * @throws NullPointerException if textID is null
	 */
	public String getRawText (String locale, String textID) {
		if(locale == null) {
			throw new UnregisteredLocaleException("Null locale when attempting to fetch text id: " + textID);
		}
		if(locale.equals(currentLocale)) {
			return (String)currentLocaleData.get(textID);
		} else {
			return (String)getLocaleMap(locale).get(textID);
		}
	}
	
	/* === MANAGING LOCALIZABLE OBSERVERS === */
	
	/**
	 * Register a Localizable to receive updates when the locale is changed. If the Localizable is already
	 * registered, nothing happens. If a locale is currently set, the new Localizable will receive an
	 * immediate 'locale changed' event.
	 * 
	 * @param l Localizable to register.
	 */
	public void registerLocalizable (Localizable l) {
		if (!observers.contains(l)) {
			observers.addElement(l);
			if (currentLocale != null) {
				l.localeChanged(currentLocale, this);
			}
		}
	}
	
	/**
	 * Unregister an Localizable from receiving locale change updates. No effect if the Localizable was never
	 * registered in the first place.
	 * 
	 * @param l Localizable to unregister.
	 */
	public void unregisterLocalizable (Localizable l) {
		observers.removeElement(l);
	}
	
	/**
	 * Unregister all ILocalizables.
	 */
	public void unregisterAll () {
		observers.removeAllElements();
	}
	
	/**
	 * Send a locale change update to all registered ILocalizables.
	 */
	private void alertLocalizables () {
		for (Enumeration e = observers.elements(); e.hasMoreElements(); )
			((Localizable)e.nextElement()).localeChanged(currentLocale, this);
	}
	
	/* === Managing Arguments === */
	
	private static String arg(String in) {
		return "${" + in + "}";
	}
	
	public static Vector getArgs (String text) {
		Vector args = new Vector();
		int i = text.indexOf("${");
		while (i != -1) {
			int j = text.indexOf("}", i);
			if (j == -1) {
				System.err.println("Warning: unterminated ${...} arg");
				break;
			}
			
			String arg = text.substring(i + 2, j);
			if (!args.contains(arg)) {
				args.addElement(arg);
			}
			
			i = text.indexOf("${", j + 1);
		}
		return args;
	}
	
	public static String processArguments(String text, Hashtable args) {
		int i = text.indexOf("${");
		while (i != -1) {
			int j = text.indexOf("}", i);
			if (j == -1) {
				System.err.println("Warning: unterminated ${...} arg");
				break;
			}

			String argName = text.substring(i + 2, j);
			String argVal = (String)args.get(argName);
			if (argVal != null) {
				text = text.substring(0, i) + argVal + text.substring(j + 1);
				j = i + argVal.length() - 1;
			}
			
			i = text.indexOf("${", j + 1);
		}
		return text;
	}
	
	public static String processArguments(String text, String[] args) {
		String working = text;
		int currentArg = 0;
		while(working.indexOf("${") != -1 && args.length > currentArg) {
			String value = extractValue(text, args);
			if(value == null) {
				value = args[currentArg];
				currentArg++;
			}
			working = replaceFirstValue(working, value);
		}
		return working;
	}
	
	private static String extractValue(String text, String[] args) {
		//int start = text.indexOf("${");
		//int end = text.indexOf("}");
		
		//String index = text.substring(start + 2, end);
		//Search for that string in the current locale, updating any arguments.
		return null;
	}
	
	private static String replaceFirstValue(String text, String value) {
		int start = text.indexOf("${");
		int end = text.indexOf("}");
		
		return text.substring(0,start) + value + text.substring(end + 1, text.length()); 
	}

	/* === (DE)SERIALIZATION === */
	
	/**
	 * Read the object from stream.
	 */
	public void readExternal(DataInputStream dis, PrototypeFactory pf) throws IOException, DeserializationException {
		fallbackDefaultLocale = ExtUtil.readBool(dis);
		fallbackDefaultForm = ExtUtil.readBool(dis);
		localeResources = (OrderedHashtable)ExtUtil.read(dis, new ExtWrapMap(String.class, new ExtWrapListPoly(), true),	pf);;
		locales = (Vector)ExtUtil.read(dis, new ExtWrapList(String.class));
		setDefaultLocale((String)ExtUtil.read(dis, new ExtWrapNullable(String.class), pf));
		String currentLocale = (String)ExtUtil.read(dis, new ExtWrapNullable(String.class), pf);
		if (currentLocale != null) {
			setLocale(currentLocale);
		}
	}
	
	/**
	 * Write the object to stream.
	 */
	public void writeExternal(DataOutputStream dos) throws IOException {
		ExtUtil.writeBool(dos, fallbackDefaultLocale);
		ExtUtil.writeBool(dos, fallbackDefaultForm);
		ExtUtil.write(dos, new ExtWrapMap(localeResources, new ExtWrapListPoly()));
		ExtUtil.write(dos, new ExtWrapList(locales));
		ExtUtil.write(dos, new ExtWrapNullable(defaultLocale));
		ExtUtil.write(dos, new ExtWrapNullable(currentLocale));
	}	
}
