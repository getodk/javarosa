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
	
	private Vector<Root> runtimeRoots;
	private Vector<RawRoot> rawRoots;
	
	private ReferenceManager() {
		runtimeRoots = new Vector<Root>();
		rawRoots = new Vector<RawRoot>();
	}
	
	public static ReferenceManager _() {
		if(instance == null) {
			instance = new ReferenceManager();
		}
		return instance;
	}
	
	public RawRoot[] getRoots() {
		RawRoot[] roots = new RawRoot[runtimeRoots.size()];
		runtimeRoots.copyInto(roots);
		return roots;
	}
	
	public void addRoot(Root root) {
		if(!runtimeRoots.contains(root)) {
			runtimeRoots.addElement(root);
		}
	}
	
	public void addRawReferenceRoot(RawRoot root) {
		if(!rawRoots.contains(root)) {
			rawRoots.addElement(root);
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
	
	private RawRoot derivingRoot(String uri) throws InvalidReferenceException {
		
		//First, try any/all roots referenced at runtime.
		for(Root root : runtimeRoots) {
			if(root.derives(uri)) {
				return root;
			}
		}
		
		//Now try all of the raw connectors available 
		for(RawRoot root : rawRoots) {
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
