/**
 * 
 */
package org.javarosa.core.reference;

import java.util.Vector;


/**
 * <p>The reference manager is a singleton class which
 * is responsible for deriving reference URI's into
 * references at runtime.</p>
 * 
 * <p>Raw reference factories
 * (which are capable of actually creating fully
 * qualified reference objects) are added with the
 * addFactory() method. The most common method
 * of doing so is to implement the PrefixedRootFactory
 * as either a full class, or an anonymous inner class,
 * providing the roots available in the current environment
 * and the code for constructing a reference from them.</p>
 * 
 * <p>RootTranslators (which rely on other factories) are 
 * used to describe that a particular reference style (generally 
 * a high level reference like "jr://media/" or "jr://images/" 
 * should be translated to another available reference in this
 * environment like "jr://file/". Root Translators do not
 * directly derive references, but rather translate them to what
 * the reference should look like in the current circumstances.</p>
 * 
 * @author ctsims
 *
 */
public class ReferenceManager {
	
	private static ReferenceManager instance;
	
	private Vector<RootTranslator> translators;
	private Vector<ReferenceFactory> factories;
	private Vector<RootTranslator> sessionTranslators;
	
	private ReferenceManager() {
		translators = new Vector<RootTranslator>();
		factories = new Vector<ReferenceFactory>();
		sessionTranslators = new Vector<RootTranslator>();
	}
	
	/**
	 * @return Singleton accessor to the global
	 * ReferenceManager.
	 */
	public static ReferenceManager _() {
		if(instance == null) {
			instance = new ReferenceManager();
		}
		return instance;
	}
	
	/**
	 * @return The available reference factories
	 */
	public ReferenceFactory[] getFactories() {
		ReferenceFactory[] roots = new ReferenceFactory[translators.size()];
		translators.copyInto(roots);
		return roots;
	}
	
	/**
	 * Adds a new Translator to the current environment.
	 * @param translator
	 */
	public void addRootTranslator(RootTranslator translator) {
		if(!translators.contains(translator)) {
			translators.addElement(translator);
		}
	}
	
	/**
	 * Adds a factory for deriving reference URI's into references
	 * @param factory A raw ReferenceFactory capable of creating 
	 * a reference.
	 */
	public void addReferenceFactory(ReferenceFactory factory) {
		if(!factories.contains(factory)) {
			factories.addElement(factory);
		}
	}
	
	public boolean removeReferenceFactory(ReferenceFactory factory) {
		return factories.removeElement(factory);
	}
	
	/**
	 * Derives a global reference from a URI in the current environment.
	 * 
	 * @param uri The URI representing a global reference.
	 * @return A reference which is identified by the provided URI.
	 * @throws InvalidReferenceException If the current reference could
	 * not be derived by the current environment
	 */
	public Reference DeriveReference(String uri) throws InvalidReferenceException {
		return DeriveReference(uri, (String)null);
	}
	
	/**
	 * Derives a reference from a URI in the current environment.
	 * 
	 * @param uri The URI representing a reference.
	 * @param context A reference which provides context for any 
	 * relative reference accessors.
	 * @return A reference which is identified by the provided URI.
	 * @throws InvalidReferenceException If the current reference could
	 * not be derived by the current environment
	 */
	public Reference DeriveReference(String uri, Reference context) throws InvalidReferenceException {
		return DeriveReference(uri, context.getURI());
	}
	
	/**
	 * Derives a reference from a URI in the current environment.
	 * 
	 * @param uri The URI representing a reference.
	 * @param context A reference URI which provides context for any 
	 * relative reference accessors.
	 * @return A reference which is identified by the provided URI.
	 * @throws InvalidReferenceException If the current reference could
	 * not be derived by the current environment, or if the context URI
	 * is not valid in the current environment.
	 */
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
	
	/**
	 * Adds a root translator that is maintained over the course of a session. It will be globally
	 * available until the session is cleared using the "clearSession" method.  
	 * 
	 * @param translator A Root Translator that will be added to the current session
	 */
	public void addSessionRootTranslator(RootTranslator translator) {
		sessionTranslators.addElement(translator);
	}
	
	/**
	 * Wipes out all of the translators being maintained in the current session (IE: Any translators
	 * added via "addSessionRootTranslator". Used to manage a temporary set of translations for a limited
	 * amount of time. 
	 */
	public void clearSession() {
		sessionTranslators.removeAllElements();
	}
	
	private ReferenceFactory derivingRoot(String uri) throws InvalidReferenceException {
		
		//First, try any/all roots which are put in the temporary session stack
		for(RootTranslator root : sessionTranslators) {
			if(root.derives(uri)) {
				return root;
			}
		}
		
		//Now, try any/all roots referenced at runtime.
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
	
	/**
	 * @param URI
	 * @return Whether the provided URI describe a relative reference.
	 */
	public static boolean isRelative(String URI) {
		if(URI.startsWith("./")) {
			return true;
		}
		return false;
	}
}
