/*
 * Copyright (C) 2009 JavaRosa
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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.javarosa.core.util.NoLocalizedTextException;
import org.javarosa.core.util.OrderedMap;
import org.javarosa.core.util.PrefixTree;
import org.javarosa.core.util.PrefixTreeNode;
import org.javarosa.core.util.UnregisteredLocaleException;
import org.javarosa.core.util.externalizable.DeserializationException;
import org.javarosa.core.util.externalizable.ExtUtil;
import org.javarosa.core.util.externalizable.ExtWrapList;
import org.javarosa.core.util.externalizable.ExtWrapListPoly;
import org.javarosa.core.util.externalizable.ExtWrapMap;
import org.javarosa.core.util.externalizable.ExtWrapNullable;
import org.javarosa.core.util.externalizable.Externalizable;
import org.javarosa.core.util.externalizable.PrototypeFactory;

/**
 * The Localizer object maintains mappings for locale ID's and Object
 * ID's to the String values associated with them in different
 * locales.
 *
 * @author Drew Roos/Clayton Sims
 *
 */
public class Localizer implements Externalizable {
	private List<String> locales; /* List<String> */
	private OrderedMap<String, List<LocaleDataSource>> localeResources; /* String -> List<LocaleDataSource> */
	private OrderedMap<String, PrefixTreeNode> currentLocaleData; /* HashMap{ String -> String } */
	private PrefixTree stringTree;
	private String defaultLocale;
	private String currentLocale;
	private boolean fallbackDefaultLocale;
	private boolean fallbackDefaultForm;
	private List<Localizable> observers;

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
		stringTree = new PrefixTree(10);
		localeResources = new OrderedMap<String, List<LocaleDataSource>>();
		currentLocaleData = new OrderedMap<String, PrefixTreeNode>();
		locales = new ArrayList<String>(0);
		defaultLocale = null;
		currentLocale = null;
		observers = new ArrayList<Localizable>(0);
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
		if ( locale == null ) {
			throw new NullPointerException("unexpected null locale");
		}
		if (hasLocale(locale)) {
			return false;
		} else {
			locales.add(locale);
			localeResources.put(locale, new ArrayList<LocaleDataSource>(1));
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
		return locales.toArray(data);
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
									 : locales.get((locales.indexOf(currentLocale) + 1) % locales.size());
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
		if (!hasLocale(currentLocale)) {
			throw new UnregisteredLocaleException("Attempted to set to a locale that is not defined. Attempted Locale: " + currentLocale);
		}

		boolean alert = false;
		if (!currentLocale.equals(this.currentLocale)) {
			this.currentLocale = currentLocale;
			alert = true;
		}
		loadCurrentLocaleResources();
		if(alert) {
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
	private void loadTable(OrderedMap<String, PrefixTreeNode> destination, OrderedMap<String, String> source) {
    for (String key : source.keySet()) {
			destination.put(key, stringTree.addString(source.get(key)));
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
      List<LocaleDataSource> resources;
		if(localeResources.containsKey(locale)) {
			resources = localeResources.get(locale);
		} else {
			resources = new ArrayList<LocaleDataSource>(1);
		}
		resources.add(resource);
		localeResources.put(locale, resources);

		if(locale.equals(currentLocale) || locale.equals(defaultLocale)) {
			loadCurrentLocaleResources();
		}
	}

	/**
	 * Get the set of mappings for a locale.
	 *
	 * @param locale Locale
	 * @returns HashMap representing text mappings for this locale. Returns null if locale not defined or null.
	 */
	public OrderedMap<String, PrefixTreeNode> getLocaleData (String locale) {
		if(locale == null || !this.locales.contains(locale)) {
			return null;
		}
		stringTree.clear();

		//It's very important that any default locale contain the appropriate strings to localize the interface
		//for any possible language. As such, we'll keep around a table with only the default locale keys to
		//ensure that there are no localizations which are only present in another locale, which causes ugly
		//and difficult to trace errors.
		OrderedMap<String, Boolean> defaultLocaleKeys = new OrderedMap<String, Boolean>();

		//This table will be loaded with the default values first (when applicable), and then with any
		//language specific translations overwriting the existing values.
		OrderedMap<String, PrefixTreeNode> data = new OrderedMap<String, PrefixTreeNode>();

		// If there's a default locale, we load all of its elements into memory first, then allow
		// the current locale to overwrite any differences between the two.
		if (fallbackDefaultLocale && defaultLocale != null) {
         List<LocaleDataSource> defaultResources = localeResources.get(defaultLocale);
			for (int i = 0; i < defaultResources.size(); ++i) {
				loadTable(data,((LocaleDataSource)defaultResources.get(i)).getLocalizedText());
			}
      for (String key : data.keySet()) {
				defaultLocaleKeys.put(key, Boolean.TRUE);
			}
		}

      List<LocaleDataSource> resources = localeResources.get(locale);
		for(int i = 0 ; i < resources.size() ; ++i ) {
			loadTable(data,((LocaleDataSource)resources.get(i)).getLocalizedText());
		}

		//Strings are now immutable
		stringTree.seal();

		//If we're using a default locale, now we want to make sure that it has all of the keys
		//that the locale we want to use does. Otherwise, the app will crash when we switch to
		//a locale that doesn't contain the key.
		if(fallbackDefaultLocale && defaultLocale != null) {
			String missingKeys = "";
			int keysmissing = 0;
      for (String key : data.keySet()) {
				if(!defaultLocaleKeys.containsKey(key)) {
					missingKeys += key + ",";
					keysmissing++;
				}
			}
			if(keysmissing > 0) {
				//Is there a good way to localize these exceptions?
				throw new NoLocalizedTextException("Error loading locale " + locale +
						". There were " + keysmissing + " keys which were contained in this locale, but were not " +
						"properly registered in the default Locale. Any keys which are added to a locale should always " +
						"be added to the default locale to ensure appropriate functioning.\n" +
						"The missing translations were for the keys: " + missingKeys,missingKeys, defaultLocale);
			}
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
	public OrderedMap<String, PrefixTreeNode> getLocaleMap (String locale) {
		OrderedMap<String, PrefixTreeNode> mapping = getLocaleData(locale);
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
		List<LocaleDataSource> resources = localeResources.get(locale);
      for (LocaleDataSource source : resources) {
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
		locales.remove(locale);
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
			throw new NoLocalizedTextException("The Localizer could not find a definition for ID: " + textID + " in the '" + currentLocale + "' locale.", textID, currentLocale);
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
	public String getText (String textID, HashMap<String,String> args) {
		String text = getText(textID, currentLocale);
		if(text != null) {
			text = processArguments(text, args);
		} else {
			throw new NoLocalizedTextException("The Localizer could not find a definition for ID: " + textID + " in the '" + currentLocale + "' locale.",textID, currentLocale);
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
	    	throw new NoLocalizedTextException("Can't find localized text for current locale! text id: [" + textID + "] locale: ["+currentLocale+"]", textID, currentLocale);
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
		//Update: We handle default text without forms without needing to do this. We still need it for default text with default forms, though
		if (text == null && fallbackDefaultLocale && !locale.equals(defaultLocale) && defaultLocale != null && fallbackDefaultForm)
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
		if(textID == null) {
			throw new NullPointerException("Null textId passed to localizer");
		}
		if(locale.equals(currentLocale)) {
			PrefixTreeNode data = currentLocaleData.get(textID);
			return data == null ? null : data.render();
		} else {
			PrefixTreeNode data = getLocaleMap(locale).get(textID);
			return data == null ? null : data.render();
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
			observers.add(l);
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
		observers.remove(l);
	}

	/**
	 * Unregister all ILocalizables.
	 */
	public void unregisterAll () {
		observers.clear();
	}

	/**
	 * Send a locale change update to all registered ILocalizables.
	 */
	private void alertLocalizables () {
      for (Localizable observer : observers) {
         observer.localeChanged(currentLocale, this);
      }
	}

	/* === Managing Arguments === */

	private static String arg(String in) {
		return "${" + in + "}";
	}

	public static List<String> getArgs (String text) {
      List<String> args = new ArrayList<String>();
		int i = text.indexOf("${");
		while (i != -1) {
			int j = text.indexOf("}", i);
			if (j == -1) {
				System.err.println("Warning: unterminated ${...} arg");
				break;
			}

			String arg = text.substring(i + 2, j);
			if (!args.contains(arg)) {
				args.add(arg);
			}

			i = text.indexOf("${", j + 1);
		}
		return args;
	}

	public static String processArguments(String text, HashMap<String,String> args) {
		int i = text.indexOf("${");
		while (i != -1) {
			int j = text.indexOf("}", i);
			if (j == -1) {
				System.err.println("Warning: unterminated ${...} arg");
				break;
			}

			String argName = text.substring(i + 2, j);
			String argVal = args.get(argName);
			if (argVal != null) {
				text = text.substring(0, i) + argVal + text.substring(j + 1);
				j = i + argVal.length() - 1;
			}

			i = text.indexOf("${", j + 1);
		}
		return text;
	}

	public static String processArguments(String text, String[] args) {
		return processArguments(text, args, 0);
	}

	// enhanced to not re-process substituted text
	public static String processArguments(String text, String[] args, int currentArg) {
		String working = text;
		if(working.indexOf("${") != -1 && args.length > currentArg) {
			int index = extractNextIndex(working, args);
			if(index == -1) {
				index = currentArg;
				currentArg++;
			}
			String value = args[index];
			String[] replaced = replaceFirstValue(working, value);
			return replaced[0] + processArguments(replaced[1], args, currentArg);
		} else {
			return working;
		}
	}


	public static String clearArguments(String text) {
      List<String> v = getArgs(text);
		String[] empty = new String[v.size()];
		for(int i =0 ; i < empty.length ; ++i) {
			empty[i] = "";
		}
		return processArguments(text, empty);
	}

	private static int extractNextIndex(String text, String[] args) {
		int start = text.indexOf("${");
		int end = text.indexOf("}", start);

		if(start != -1 && end != -1 ) {
			String val = text.substring(start + "${".length(), end);
			try {
				int index = Integer.parseInt(val);
				if(index >= 0 && index < args.length) {
					return index;
				}
			} catch(NumberFormatException nfe) {
				return -1;
			}
		}

		return -1;
	}

	private static String[] replaceFirstValue(String text, String value) {
		int start = text.indexOf("${");
		int end = text.indexOf("}", start);

		return new String[] {text.substring(0,start) + value, text.substring(end + 1, text.length())};
	}

	/* === (DE)SERIALIZATION === */

	/**
	 * Read the object from stream.
	 */
	public void readExternal(DataInputStream dis, PrototypeFactory pf) throws IOException, DeserializationException {
		fallbackDefaultLocale = ExtUtil.readBool(dis);
		fallbackDefaultForm = ExtUtil.readBool(dis);
		localeResources = (OrderedMap<String,List<LocaleDataSource>>)ExtUtil.read(dis, new ExtWrapMap(String.class, new ExtWrapListPoly(), ExtWrapMap.TYPE_ORDERED),	pf);;
		locales = (List<String>)ExtUtil.read(dis, new ExtWrapList(String.class));
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
