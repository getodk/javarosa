/*
 * ILocalizer.java
 *
 * Created on April 28, 2008, 10:01 AM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.javarosa.dtree.i18n;

import org.javarosa.clforms.util.SimpleOrderedHashtable;


public interface ILocalizer {
  public String getText(String textId); 
  public String getText(String textId, String locale);

  public SimpleOrderedHashtable getSelectMap(String selectMapId);
  public SimpleOrderedHashtable getSelectMap(String selectMapId, String locale);
}
