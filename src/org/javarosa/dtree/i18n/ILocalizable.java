/*
 * ILocalizable.java
 *
 * Created on April 28, 2008, 10:06 AM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.javarosa.dtree.i18n;

public interface ILocalizable {
    
   public void localeChanged(String locale, ILocalizer localizer);

}
