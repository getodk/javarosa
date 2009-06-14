package org.javarosa.core.services;

import java.util.Vector;

import org.javarosa.core.services.properties.IPropertyRules;

/**
 * An IProperty Manager is responsible for setting and retrieving name/value pairs
 * 
 * @author Yaw Anokwa
 *
 */
public interface IPropertyManager extends IService {

    public String getName();
    public Vector getProperty(String propertyName);
    public void setProperty(String propertyName, String propertyValue);
    public void setProperty(String propertyName, Vector propertyValue);
    public String getSingularProperty(String propertyName);
    public void addRules(IPropertyRules rules);
    public Vector getRules();   
}
