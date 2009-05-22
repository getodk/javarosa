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
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.NoSuchElementException;
import java.util.Vector;

import org.javarosa.core.util.OrderedHashtable;
import org.javarosa.core.util.externalizable.DeserializationException;
import org.javarosa.core.util.externalizable.ExtUtil;
import org.javarosa.core.util.externalizable.ExtWrapList;
import org.javarosa.core.util.externalizable.ExtWrapMap;
import org.javarosa.core.util.externalizable.ExtWrapNullable;
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
	private OrderedHashtable localeResources; /* String -> Vector<String> */
	private OrderedHashtable localeData; /* String -> Hashtable{ String -> String } */
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
		localeData = new OrderedHashtable();
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
			return (ExtUtil.equals(localeData, l.localeData) &&
					ExtUtil.equals(locales, locales) &&
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
			localeData.put(locale, new OrderedHashtable());
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
	 * @throws NoSuchElementException If locale is null or not defined.
	 */
	public void setLocale (String currentLocale) {
		if (!hasLocale(currentLocale))
			throw new NoSuchElementException("Attempted to set to a locale that is not defined");
		
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
	 * @throws NoSuchElementException If locale is not defined.
	 */
	public void setDefaultLocale (String defaultLocale) {
		if (defaultLocale != null && !hasLocale(defaultLocale))
			throw new NoSuchElementException("Attempted to set default to a locale that is not defined");
		
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
	
	private void loadCurrentLocaleResources() {
		this.currentLocaleData.clear();
		OrderedHashtable table = (OrderedHashtable)this.localeData.get(currentLocale);
		loadTable(currentLocaleData, table);
		Vector resources = (Vector)localeResources.get(currentLocale);
		for(int i = 0 ; i < resources.size() ; ++i ) {
			loadTable(currentLocaleData, loadLocaleResource((String)resources.elementAt(i)));
		}
	}
	
	private void loadTable(OrderedHashtable table, OrderedHashtable source) {
		for(Enumeration en = source.keys(); en.hasMoreElements(); ) {
			String key = (String)en.nextElement();
			table.put(key, (String)source.get(key));
		}
	}

	private OrderedHashtable loadLocaleResource(String resourceName) {
		InputStream is = System.class.getResourceAsStream(resourceName);
		// TODO: This might very well fail. Best way to handle?
		OrderedHashtable locale = new OrderedHashtable();
		int chunk = 100;
		InputStreamReader isr = new InputStreamReader(is);
		boolean done = false;
		char[] cbuf = new char[chunk];
		int offset = 0;
		int curline = 0;
		
		try {
			while (!done) {
				int read = isr.read(cbuf, offset, chunk - offset);
				if(read == -1) {
					done = true;
					break;
				}
				String line = "";
				for(int i = 0 ; i < read ; ++i) {
					//TODO: Endline formats?
					if(cbuf[i] == '\n') {
						//Newline. process our string and start the next one.
						//clear comments
						line = line.substring(0, line.indexOf("#"));
						if(line.indexOf('=') == -1) {
							// TODO: Invalid line. Empty lines are fine, especially with comments,
							// but it might be hard to get all of those.
							if(line.trim().equals("")) {
								//Empty Line
							} else {
								System.out.println("Invalid line (#" + curline + ") read: " + line);
							}
						} else {
							locale.put(line.substring(0, line.indexOf('=')), line.substring(line.indexOf('='),line.length()));
							line = "";
						}
					} else {
						line += cbuf[i];
					}
					curline ++;
				}
				if(line.equals("")) {
					//Start at the beginning, we read a clean line.
					offset = 0;
				} else {
					//We have some characters that are in the middle of a line. Populate the array and prepare
					//the offset.
					for(int j = 0; j < line.length(); ++j) {
						cbuf[j] = line.charAt(j);
					}
					offset = line.length();
				}
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return locale;
	}

	/* === MANAGING LOCALE DATA (TEXT MAPPINGS) === */
	
	public void registerLocaleResource (String locale, String resourceURI) {
		Vector resources = new Vector();
		if(localeResources.contains(locale)) {
			resources = (Vector)localeResources.get(locale);
		}
		resources.addElement(resourceURI);
		localeResources.put(locale, resources);
	}
	
	/**
	 * Populate a locale with a set of mappings. Will overwrite any existing mappings for that locale.
	 * 
	 * @param locale Locale. Need not be defined (this method will define it). Must not be null.
	 * @param mappings Text mappings for this locale. A hashtable that maps ([text handle] -> [localized text]).
	 * Must not be null. Note: a text handle is a text ID appended with an optional text form.
	 * @return Whether an existing set of mappings (excluding the empty set) for this locale was overwritten.
	 * @throws NullPointerException if locale or mappings is null
	 */
	public boolean setLocaleData (String locale, OrderedHashtable mappings) {
		OrderedHashtable origMapping = (OrderedHashtable)localeData.get(locale);
		boolean overwritten = (origMapping == null ? false : origMapping.size() > 0);
		
		if(!locales.contains(locale)) {
			locales.addElement(locale);
		}
		
		localeData.put(locale, mappings);
		return overwritten;
	}
	
	/**
	 * Get the set of mappings for a locale.
	 * 
	 * @param locale Locale
	 * @returns Hashtable representing text mappings for this locale. Returns null if locale not defined or null.
	 */
	public OrderedHashtable getLocaleData (String locale) {
		return (locale == null ? null : (OrderedHashtable)localeData.get(locale));
	}

	/**
	 * Get the mappings for a locale, but throw an exception if locale is not defined.
	 * 
	 * @param locale Locale
	 * @return Text mappings for locale.
	 * @throws NoSuchElementException If locale is not defined or null.
	 */
	public OrderedHashtable getLocaleMap (String locale) {
		OrderedHashtable mapping = getLocaleData(locale);
		if (mapping == null)
			throw new NoSuchElementException("Attempted to access an undefined locale.");
		return mapping;
	}
	
	/**
	 * Set a text mapping for a single text handle for a given locale.
	 * 
	 * @param locale Locale. Must be defined and not null.
	 * @param textID Text handle. Must not be null. Need not be previously defined for this locale.
	 * @param text Localized text for this text handle and locale. Will overwrite any previous mapping, if one existed.
	 * If null, will remove any previous mapping for this text handle, if one existed.
	 * @throws NoSuchElementException If locale is not defined or null.
	 * @throws NullPointerException if textID is null
	 */
	public void setLocaleMapping (String locale, String textID, String text) {
		if (text == null) {
			getLocaleMap(locale).remove(textID);			
		} else {
			getLocaleMap(locale).put(textID, text);
		}
	}
	
	/**
	 * Determine whether a locale has a mapping for a given text handle. Only tests the specified locale and form; does
	 * not fallback to any default locale or text form.
	 * 
	 * @param locale Locale. Must be defined and not null.
	 * @param textID Text handle.
	 * @return True if a mapping exists for the text handle in the given locale.
	 * @throws NoSuchElementException If locale is not defined.
	 */
	public boolean hasMapping (String locale, String textID) {
		OrderedHashtable localeData = getLocaleMap(locale);
		return (textID == null ? false : localeData.get(textID) != null);
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
		localeData.remove(locale);
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
	 * @throws NoSuchElementException If current locale is not set.
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
	 * @return Localized text. If no text is found after using all fallbacks, return null.
	 * @throws NoSuchElementException If current locale is not set.
	 * @throws NullPointerException if textID is null
	 */
	public String getText (String textID, String[] args) {
		String text = getText(textID, currentLocale);
		if(text != null) {
			text = processArguments(text, args);
		}
		return text;
	}
	/**
	 * Retrieve the localized text for a text handle in the current locale. See getText(String, String) for details.
	 *
	 * @param textID Text handle (text ID appended with optional text form). Must not be null.
	 * @param args arguments for string variables.
	 * @return Localized text. If no text is found after using all fallbacks, return null.
	 * @throws NoSuchElementException If current locale is not set.
	 * @throws NullPointerException if textID is null
	 */
	public String getText (String textID, Hashtable args) {
		String text = getText(textID, currentLocale);
		if(text != null) {
			text = processArguments(text, args);
		}
		return text;
	}
	
	/**
	 * Retrieve localized text for a text handle in the current locale. Like getText(String), however throws exception
	 * if no localized text is found.
	 * 
	 * @param textID Text handle (text ID appended with optional text form). Must not be null.
	 * @return Localized text
	 * @throws NoSuchElementException If current locale is not set, or if no localized text is found.
	 * @throws NullPointerException if textID is null
	 */
	public String getLocalizedText (String textID) {
	    String text = getText(textID);
	    if (text == null)
	    	throw new NoSuchElementException("Can't find localized text for current locale! text id: [" + textID + "]");
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
	 * @throws NoSuchElementException If the locale is not defined or null.
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
	 * @throws NoSuchElementException If the locale is not defined or null.
	 * @throws NullPointerException if textID is null
	 */
	public String getRawText (String locale, String textID) {
		if(locale == null) {
			throw new NoSuchElementException();
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
		while(working.indexOf("${") != -1 || args.length > currentArg) {
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
		int start = text.indexOf("${");
		int end = text.indexOf("}");
		
		String index = text.substring(start + 2, end);
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
		localeData = (OrderedHashtable)ExtUtil.read(dis, new ExtWrapMap(String.class, new ExtWrapMap(String.class, String.class, true), true), pf);
		localeResources = (OrderedHashtable)ExtUtil.read(dis, new ExtWrapMap(String.class, new ExtWrapList(String.class), true), pf);
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
		ExtUtil.write(dos, new ExtWrapMap(localeData, new ExtWrapMap(String.class, String.class, true)));
		ExtUtil.write(dos, new ExtWrapMap(localeResources, new ExtWrapList(String.class)));
		ExtUtil.write(dos, new ExtWrapList(locales));
		ExtUtil.write(dos, new ExtWrapNullable(defaultLocale));
		ExtUtil.write(dos, new ExtWrapNullable(currentLocale));
	}	
}
