package org.javarosa.core.model.utils;

/**
 * Localizable objects are able to update their text
 * based on the current locale.
 * 
 * @author Drew Roos
 *
 */
public interface Localizable {
	/**
	 * Updates the current object with the locate given.
	 * @param locale
	 * @param localizer
	 */
	public void localeChanged(String locale, Localizer localizer);
}
