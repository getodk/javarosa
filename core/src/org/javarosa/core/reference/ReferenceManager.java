/**
 * 
 */
package org.javarosa.core.reference;

import java.util.Vector;


/**
 * @author ctsims
 *
 */
public class ReferenceManager {
	
	private static ReferenceManager instance;
	
	private Vector<RootTranslator> translators;
	private Vector<ReferenceFactory> factories;
	
	private ReferenceManager() {
		translators = new Vector<RootTranslator>();
		factories = new Vector<ReferenceFactory>();
	}
	
	public static ReferenceManager _() {
		if(instance == null) {
			instance = new ReferenceManager();
		}
		return instance;
	}
	
	public ReferenceFactory[] getFactories() {
		ReferenceFactory[] roots = new ReferenceFactory[translators.size()];
		translators.copyInto(roots);
		return roots;
	}
	
	public void addRootTranslator(RootTranslator translator) {
		if(!translators.contains(translator)) {
			translators.addElement(translator);
		}
	}
	
	public void addReferenceFactory(ReferenceFactory factory) {
		if(!factories.contains(factory)) {
			factories.addElement(factory);
		}
	}
	
	public Reference DeriveReference(String uri) throws InvalidReferenceException {
		return DeriveReference(uri, (String)null);
	}
	
	public Reference DeriveReference(String uri, Reference context) throws InvalidReferenceException {
		return DeriveReference(uri, context.getURI());
	}
	
	public Reference DeriveReference(String uri, String context) throws InvalidReferenceException {
		
		//Relative URI's need to determine their context first.
		if(isRelative(uri)) {
			
			//Clean up the relative reference to lack any leading separators.
			if(uri.startsWith("./")) {
				uri = uri.substring(2);
			}
			
			if(context == null ) {
				throw new RuntimeException("Attempted to retrieve local reference with no context");
			} else {
				return derivingRoot(context).derive(uri, context);
			}
		} else {
			return derivingRoot(uri).derive(uri);
		}
	}
	
	private ReferenceFactory derivingRoot(String uri) throws InvalidReferenceException {
		
		//First, try any/all roots referenced at runtime.
		for(RootTranslator root : translators) {
			if(root.derives(uri)) {
				return root;
			}
		}
		
		//Now try all of the raw connectors available 
		for(ReferenceFactory root : factories) {
			if(root.derives(uri)) {
				return root;
			}
		}
		
		throw new InvalidReferenceException("No reference could be created for URI " + uri, uri);
	}
	
	public static boolean isRelative(String URI) {
		if(URI.startsWith("./")) {
			return true;
		}
		return false;
	}
}
