package org.javarosa.core.util;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

import org.javarosa.core.util.db.Persistent;
import org.javarosa.core.util.db.PersistentHelper;


/**
 * Stores user settings.
 * 
 * @author Daniel
 *
 */
public class Settings implements Persistent{
	
	private String storageName = null;
	private Hashtable settings = new Hashtable();
	
	/** This should be called only by the deserializer. */
	public Settings(){
		
	}
	
	/**
	 * Constructs a settings storage with a custom storageName.
	 * 
	 * @param storageName the storageName of storage.
	 */
	public Settings(String storageName){
		setStorageName(storageName);
	}
	
	/**
	 * Constructs a new settings object.
	 * 
	 * @param loadSettings - set to true if you want to load existing settings.
	 */
	public Settings(String storageName, boolean loadSettings){
		setStorageName(storageName);
		loadSettings();
	}
	
	private String getStorageName() {
		return storageName;
	}

	private void setStorageName(String name) {
		this.storageName = name;
	}

	private Hashtable getSettings() {
		return settings;
	}

	private void setSettings(Hashtable settings) {
		this.settings = settings;
	}

	public Enumeration keys(){
		return settings.keys();
	}
	
	public boolean hasMoreElements(){
		return settings.keys().hasMoreElements();
	}
	
	public String nextElement(){
		return (String)settings.keys().nextElement();
	}
	
	public String getSetting(String key){
		return (String)settings.get(key);
	}
	
	public void setSetting(String key, String value){
		settings.put(key, value);
	}
	
	public void deleteSetting(String key){
		settings.remove(key);
	}
	
	public void deleteAllSettings(){
		settings.clear();
	}
	
	public void loadSettings(){
		Storage store = StorageFactory.getStorage(getStorageName(),null);
		Vector vect = store.read(new Settings().getClass());
		if(vect != null && vect.size() > 0)
			settings = ((Settings)vect.elementAt(0)).settings;
		
		if(settings == null)
			settings = new Hashtable();
	}
	
	public void saveSettings(){
		Storage store = StorageFactory.getStorage(getStorageName(),null);
		store.delete();
		store.addNew(this);
	}
	
	public void write(DataOutputStream dos) throws IOException{
    	PersistentHelper.write(settings, dos);
    }
    
	public void read(DataInputStream dis) throws IOException,InstantiationException,IllegalAccessException{
    	settings = PersistentHelper.read(dis);		
    }

}
