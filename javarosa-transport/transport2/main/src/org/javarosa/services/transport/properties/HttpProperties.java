package org.javarosa.services.transport.properties;

import java.util.Hashtable;

public class HttpProperties {
	
	private static Hashtable properties = new Hashtable();
	
	public static void addProperty(String key, String value){
		properties.put(key, value);
	}
	
	public static Object getProperty(String key){
		return properties.get(key);
	}
	
	public static void removeProperty(String key){
		properties.remove(key);
	}
}
