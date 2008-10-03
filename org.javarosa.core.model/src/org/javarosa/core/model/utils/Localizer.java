package org.javarosa.core.model.utils;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Enumeration;
import java.util.NoSuchElementException;
import java.util.Vector;

import org.javarosa.core.util.OrderedHashtable;
import org.javarosa.core.util.externalizable.DeserializationException;
import org.javarosa.core.util.externalizable.ExtUtil;
import org.javarosa.core.util.externalizable.Externalizable;
import org.javarosa.core.util.externalizable.ExternalizableHelperDeprecated;
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
	private OrderedHashtable localeData; /* String -> Hashtable{ String -> String } */
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
			localeData.put(locale, new OrderedHashtable());
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
	 * @param locale Locale
	 * @return Whether the locale is defined. False if null
	 */
	public boolean hasLocale (String locale) {
		return (locale == null ? false : localeData.get(locale) != null);
	}
	
	/**
	 * Return the next locale in order, for cycling through locales.
	 * 
	 * @return Next locale following the current locale (if the current locale is the last, cycle back to the beginning).
	 * If the current locale is not set, return the default locale. If the default locale is not set, return null.
	 */
	public String getNextLocale () {
		return currentLocale == null ? defaultLocale
									 : (String)localeData.keyAt((localeData.indexOfKey(currentLocale) + 1) % localeData.size());
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
	 * @throws NullPointerException if locale or mappings is null
	 */
	public boolean setLocaleData (String locale, OrderedHashtable mappings) {
		OrderedHashtable origMapping = (OrderedHashtable)localeData.get(locale);
		boolean overwritten = (origMapping == null ? false : origMapping.size() > 0);
		
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
	 * @throws NullPointerException if textID is null
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
	 * Get text for locale and exact text ID only, not using any fallbacks
	 * 
	 * @param locale Locale. Must be defined and not null.
	 * @param textID Text handle (text ID appended with optional text form). Must not be null.
	 * @return Localized text. Return null if none found.
	 * @throws NoSuchElementException If the locale is not defined or null.
	 * @throws NullPointerException if textID is null
	 */
	public String getRawText (String locale, String textID) {
		return (String)getLocaleMap(locale).get(textID);
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
	public void readExternal(DataInputStream dis, PrototypeFactory pf) throws IOException, DeserializationException {
		if(!ExternalizableHelperDeprecated.isEOF(dis)){
			fallbackDefaultLocale = ExternalizableHelperDeprecated.readBoolean(dis).booleanValue();
			fallbackDefaultForm = ExternalizableHelperDeprecated.readBoolean(dis).booleanValue();
			localeData = ExternalizableHelperDeprecated.readExternalCompoundSOH(dis);
			if (localeData == null)
				localeData = new OrderedHashtable();
			setDefaultLocale(ExternalizableHelperDeprecated.readUTF(dis));
			String currentLocale = ExternalizableHelperDeprecated.readUTF(dis);
			if(currentLocale != null) {
				setLocale(currentLocale);
			}
		}	
	}
	
	/**
	 * Write the object to stream.
	 */
	public void writeExternal(DataOutputStream dos) throws IOException {
		ExternalizableHelperDeprecated.writeBoolean(dos, new Boolean(fallbackDefaultLocale));		
		ExternalizableHelperDeprecated.writeBoolean(dos, new Boolean(fallbackDefaultForm));
		ExternalizableHelperDeprecated.writeExternalCompoundSOH(localeData, dos);
		ExternalizableHelperDeprecated.writeUTF(dos, defaultLocale);
		ExternalizableHelperDeprecated.writeUTF(dos, currentLocale);		
	}	
}
