package org.javarosa.core.model.utils;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Enumeration;
import java.util.NoSuchElementException;
import java.util.Vector;

import org.javarosa.core.services.storage.utilities.Externalizable;

public class Localizer implements Externalizable {
	private SimpleOrderedHashtable localeData; /* String -> Hashtable{ String -> String } */
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
		localeData = new SimpleOrderedHashtable();
		defaultLocale = null;
		currentLocale = null;
		observers = new Vector();
		this.fallbackDefaultLocale = fallbackDefaultLocale;
		this.fallbackDefaultForm = fallbackDefaultForm;
	}
	
	/* === INFORMATION ABOUT AVAILABLE LOCALES === */
	
	/**
	 * Create a new locale (with no mappings). Do nothing if the locale is already defined.
	 * 
	 * @param locale Locale to add. Must not be null.
	 * @return True if the locale was not already defined.
	 */
	public boolean addAvailableLocale (String locale) {
		if (hasLocale(locale)) {
			return false;
		} else {
			localeData.put(locale, new SimpleOrderedHashtable());
			return true;
		}
	}
	
	/**
	 * Get a list of defined locales.
	 * 
	 * @return Array of defined locales, in order they were created.
	 */
	public String[] getAvailableLocales () {
		String[] locales = new String[localeData.size()];
		for (int i = 0; i < locales.length; i++) {
			locales[i] = (String)localeData.keyAt(i);
		}
		return locales;
	}
	
	/**
	 * Get whether a locale is defined. The locale need not have any mappings.
	 * 
	 * @param locale Locale. Must not be null.
	 * @return Whether the locale is defined.
	 */
	public boolean hasLocale (String locale) {
		return localeData.get(locale) != null;
	}
	
	/**
	 * Return the next locale in order, for cycling through locales.
	 * 
	 * @return Next locale following the current locale (if the current locale is the last, cycle back to the beginning).
	 * If the current locale is not set, return the default locale. If the default locale is not set, return null.
	 */
	public String getNextLocale () {
		return currentLocale == null ? defaultLocale
									 : (String)localeData.keyAt((localeData.getIndex(currentLocale) + 1) % localeData.size());
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
	 * @throws NoSuchElementException If locale is not defined.
	 */
	public void setLocale (String currentLocale) {
		if (!hasLocale(currentLocale))
			throw new NoSuchElementException("Attempted to set to a locale that is not defined");
		
		if (!currentLocale.equals(this.currentLocale)) {
			this.currentLocale = currentLocale;
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
	
	/* === MANAGING LOCALE DATA (TEXT MAPPINGS) === */
	
	/**
	 * Populate a locale with a set of mappings. Will overwrite any existing mappings for that locale.
	 * 
	 * @param locale Locale. Need not be defined (this method will define it). Must not be null.
	 * @param mappings Text mappings for this locale. A hashtable that maps ([text handle] -> [localized text]).
	 * Must not be null. Note: a text handle is a text ID appended with an optional text form.
	 * @return Whether an existing set of mappings (excluding the empty set) for this locale was overwritten.
	 */
	public boolean setLocaleData (String locale, SimpleOrderedHashtable mappings) {
		SimpleOrderedHashtable origMapping = (SimpleOrderedHashtable)localeData.get(locale);
		boolean overwritten = (origMapping == null ? false : origMapping.size() > 0);
		
		localeData.put(locale, mappings);
		return overwritten;
	}
	
	/**
	 * Get the set of mappings for a locale.
	 * 
	 * @param locale Locale. Must not be null.
	 * @returns Hashtable representing text mappings for this locale. Returns null if locale not defined.
	 */
	public SimpleOrderedHashtable getLocaleData (String locale) {
		return (SimpleOrderedHashtable)localeData.get(locale);
	}

	/**
	 * Get the mappings for a locale, but throw an exception if locale is not defined.
	 * 
	 * @param locale Locale. Must not be null.
	 * @return Text mappings for locale.
	 * @throws NoSuchElementException If locale is not defined.
	 */
	private SimpleOrderedHashtable getLocaleMap (String locale) {
		SimpleOrderedHashtable mapping = getLocaleData(locale);
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
	 * @throws NoSuchElementException If locale is not defined.
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
	 * @param textID Text handle. Must not be null.
	 * @return True if a mapping exists for the text handle in the given locale.
	 * @throws NoSuchElementException If locale is not defined.
	 */
	public boolean hasMapping (String locale, String textID) {
		return getLocaleMap(locale).get(textID) != null;
	}
	
	/**
	 * Undefine a locale and remove all its data. Cannot be called on the current locale. If called on the default
	 * locale, no default locale will be set afterward.
	 * 
	 * @param locale Locale to remove. Must not be null. Need not be defined. Must not be the current locale.
	 * @return Whether the locale existed in the first place.
	 * @throws IllegalArgumentException If locale is the current locale.
	 */
	public boolean destroyLocale (String locale) {
		if (locale.equals(currentLocale))
			throw new IllegalArgumentException("Attempted to destroy the current locale");
		
		boolean removed = hasLocale(locale);
		localeData.remove(locale);

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
	 */
	public String getText (String textID) {
		return getText(textID, currentLocale);
	}
	
	/**
	 * Retrieve localized text for a text handle in the current locale. Like getText(String), however throws exception
	 * if no localized text is found.
	 * 
	 * @param textID Text handle (text ID appended with optional text form). Must not be null.
	 * @return Localized text
	 * @throws NoSuchElementException If current locale is not set, or if no localized text is found.
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
	 * @throws NoSuchElementException If the locale is not defined.
	 */
	public String getText (String textID, String locale) {
		String text = (String)getLocaleMap(locale).get(textID);
		if (text == null && fallbackDefaultForm && textID.indexOf(";") != -1)
			text = (String)getLocaleMap(locale).get(textID.substring(0, textID.indexOf(";")));
		if (text == null && fallbackDefaultLocale && !locale.equals(defaultLocale) && defaultLocale != null)
			text = getText(textID, defaultLocale);
		return text;
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

	/* === (DE)SERIALIZATION === */
	
	/**
	 * Read the object from stream.
	 */
	public void readExternal(DataInputStream dis) throws IOException, IllegalAccessException, InstantiationException{
		if(!ExternalizableHelper.isEOF(dis)){
			fallbackDefaultLocale = ExternalizableHelper.readBoolean(dis).booleanValue();
			fallbackDefaultForm = ExternalizableHelper.readBoolean(dis).booleanValue();
			localeData = ExternalizableHelper.readExternalSOH(dis);
			if (localeData == null)
				localeData = new SimpleOrderedHashtable();
			setDefaultLocale(ExternalizableHelper.readUTF(dis));
			setLocale(ExternalizableHelper.readUTF(dis));
		}	
	}
	
	/**
	 * Write the object to stream.
	 */
	public void writeExternal(DataOutputStream dos) throws IOException {
		ExternalizableHelper.writeBoolean(dos, new Boolean(fallbackDefaultLocale));		
		ExternalizableHelper.writeBoolean(dos, new Boolean(fallbackDefaultForm));
		ExternalizableHelper.writeExternalCompoundSOH(localeData, dos);
		ExternalizableHelper.writeUTF(dos, defaultLocale);
		ExternalizableHelper.writeUTF(dos, currentLocale);		
	}	
}
