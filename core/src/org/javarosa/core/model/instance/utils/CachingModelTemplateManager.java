package org.javarosa.core.model.instance.utils;

import java.util.Hashtable;
import java.util.Vector;

import org.javarosa.core.model.instance.FormInstance;

/**
 * Model template manager that caches the template data models in memory. Useful for when deserializing
 * many saved forms of the same form type at once.
 * 
 * Model templates are lazily loaded into the cache upon the first request for the model of that type.
 * 
 * Models stay cached until explicitly cleared.
 * 
 * Keeping too many DataModelTrees cached at once may exhaust your memory. It's best if all saved forms
 * being deserialized in bulk belong to a set of a few, known form types. It is possible to explicitly
 * set the allowed form types, such that any attempt to deserialize a form of a different type will throw
 * an error, instead of caching the new model template.
 * 
 * @author Drew Roos
 *
 */
public class CachingModelTemplateManager implements DataModelTemplateManager {

	private Hashtable<Integer, FormInstance> templateCache;
	private Vector<Integer> allowedFormTypes;
	private boolean restrictFormTypes;
	
	public CachingModelTemplateManager () {
		this(true);
	}
	
	/**
	 * 
	 * @param restrictFormTypes if true, only allowed form types will be cached; any requests for the templates
	 *     for other form types will throw an error; the list of allowed types starts out empty; register allowed
	 *     form types with addFormType(). if false, all form types will be handled and cached
	 */
	public CachingModelTemplateManager (boolean restrictFormTypes) {
		this.templateCache = new Hashtable<Integer, FormInstance>();
		this.restrictFormTypes = restrictFormTypes;
		this.allowedFormTypes = new Vector<Integer>();
	}
	
	/**
	 * Remove all model templates from the cache. Frees up memory.
	 */
	public void clearCache () {
		templateCache.clear();
	}
	
	/**
	 * Set a form type as allowed for caching. Only has an effect if this class has been set to restrict form types
	 * @param formID
	 */
	public void addFormType (int formID) {
		if (!allowedFormTypes.contains(new Integer(formID))) {
			allowedFormTypes.addElement(new Integer(formID));
		}
	}
	
	/**
	 * Empty the list of allowed form types
	 */
	public void resetFormTypes () {
		allowedFormTypes.removeAllElements();
	}
	
	/**
	 * Return the template model for the given form type. Serves the template out of the cache, if cached; fetches it
	 * fresh and caches it otherwise. If form types are restricted and the given form type is not allowed, throw an error
	 */
	public FormInstance getTemplateModel (int formID) {
		if (restrictFormTypes && !allowedFormTypes.contains(new Integer(formID))) {
			throw new RuntimeException ("form ID [" + formID + "] is not an allowed form type!");
		}
		
		FormInstance template = templateCache.get(new Integer(formID));
		if (template == null) {
			template = CompactModelWrapper.loadTemplateModel(formID);
			if (template == null) {
				throw new RuntimeException("no formdef found for form id [" + formID + "]");
			}
			templateCache.put(new Integer(formID), template);
		}
		return template;
	}

}
