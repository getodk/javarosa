/*
 * XFormsLocalizer.java
 *
 * Created on April 28, 2008, 10:41 AM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.javarosa.dtree.i18n;

import java.util.Enumeration;
import java.util.Vector;
import org.javarosa.clforms.api.Form;
import org.javarosa.clforms.util.SimpleOrderedHashtable;

public class XFormsLocaleManager {
    private static Vector availableLanguages = new Vector();
    private static int localeIndex;
    private static Vector localizables = new Vector();
    private static SimpleOrderedHashtable languageMap = new SimpleOrderedHashtable();
    
    private static String currentLocale;
    private static String defaultLocale;
    
    public static void addAvailableLocale(String locale) {
        if(availableLanguages.contains(locale)){ 
            return;
        }
        availableLanguages.addElement(locale);    
    }

    public static String[] getAvailableLocales() {
        String[] availableLocale = new String[availableLanguages.size()];
        availableLanguages.copyInto(availableLocale);
        return availableLocale;
    }

    public static String getNextLocale() {
        int size = availableLanguages.size();
        String element = null;
        if(size > localeIndex) {
            element = (String) availableLanguages.elementAt(localeIndex);
            localeIndex ++;
        } else {
            localeIndex = 0;
             element = (String) availableLanguages.elementAt(localeIndex);
        }
        return element;
    }


    public static void registerComponent(ILocalizable localizable) {
        localizables.addElement(localizable);
        
    }

    public static void unRegisterComponent(ILocalizable localizable) {
        localizables.removeElement(localizable);
    }

    public static void setLocale(String locale) {
        currentLocale = locale;
        Enumeration localizableEnum = localizables.elements();
        // System.out.println("localizabels.size() --> " + localizables.size());
        
        while(localizableEnum.hasMoreElements()){
        	ILocalizable local = (ILocalizable) localizableEnum.nextElement();
                ILocalizer localizer = XFormsLocaleManager.getLocalizer();
        	local.localeChanged(locale, localizer);
                // System.out.println("local == " + local + " localizer == " + localizer);
        } 
    }

    public static String getLocale() {
        if(currentLocale == null) {
            currentLocale = defaultLocale;
        }
        return currentLocale;
    }

    public static void loadLocale(String locale, SimpleOrderedHashtable textMappings) {
        languageMap.put(locale, textMappings);
    }

    public static void destroyLocale(String locale) {
        languageMap.remove(locale);
    }

    public static String getDefaultLocale() {
        return defaultLocale;
    }

    public static void setDefaultLocale(String locale) {
        defaultLocale = locale;
    }


    public static void setLocaleMapSetting(String locale, String textId, Object value) {
        ((SimpleOrderedHashtable)languageMap.get(locale)).put(textId, value);
        //System.out.println("locale == " + locale + " textId == " + textId + " value == " + value);
    }
    
    
    public static ILocalizer getLocalizer(){
        XFormsLocalizer localizer = null;
        if(currentLocale != null)
            localizer = new XFormsLocalizer((SimpleOrderedHashtable)languageMap.get(currentLocale));
    	
        return localizer;
    }
  
    public static ILocalizer getDefaultLocalizer(){
        XFormsLocalizer localizer = null;
        if(defaultLocale != null)
            localizer = new XFormsLocalizer((SimpleOrderedHashtable)languageMap.get(defaultLocale));
    	
        return localizer;
    }
}
