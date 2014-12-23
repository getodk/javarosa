package org.javarosa.core.model.instance.utils;

import java.util.ArrayList;
import java.util.HashMap;

import org.javarosa.core.model.instance.FormInstance;

/**
 * Instance template manager that caches the template instances in memory. Useful for when deserializing
 * many saved forms of the same form type at once.
 *
 * Instance templates are lazily loaded into the cache upon the first request for the instance of that type.
 *
 * Instances stay cached until explicitly cleared.
 *
 * Keeping too many FormInstances cached at once may exhaust your memory. It's best if all saved forms
 * being deserialized in bulk belong to a set of a few, known form types. It is possible to explicitly
 * set the allowed form types, such that any attempt to deserialize a form of a different type will throw
 * an error, instead of caching the new instance template.
 *
 * @author Drew Roos
 *
 */
public class CachingInstanceTemplateManager implements InstanceTemplateManager {

	private HashMap<Integer, FormInstance> templateCache;
	private ArrayList<Integer> allowedFormTypes;
	private boolean restrictFormTypes;

	public CachingInstanceTemplateManager () {
		this(true);
	}

	/**
	 *
	 * @param restrictFormTypes if true, only allowed form types will be cached; any requests for the templates
	 *     for other form types will throw an error; the list of allowed types starts out empty; register allowed
	 *     form types with addFormType(). if false, all form types will be handled and cached
	 */
	public CachingInstanceTemplateManager (boolean restrictFormTypes) {
		this.templateCache = new HashMap<Integer, FormInstance>();
		this.restrictFormTypes = restrictFormTypes;
		this.allowedFormTypes = new ArrayList<Integer>(0);
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
		if (!allowedFormTypes.contains(Integer.valueOf(formID))) {
			allowedFormTypes.add(Integer.valueOf(formID));
		}
	}

	/**
	 * Empty the list of allowed form types
	 */
	public void resetFormTypes () {
		allowedFormTypes.clear();
	}

	/**
	 * Return the template model for the given form type. Serves the template out of the cache, if cached; fetches it
	 * fresh and caches it otherwise. If form types are restricted and the given form type is not allowed, throw an error
	 */
	public FormInstance getTemplateInstance (int formID) {
		if (restrictFormTypes && !allowedFormTypes.contains(Integer.valueOf(formID))) {
			throw new RuntimeException ("form ID [" + formID + "] is not an allowed form type!");
		}

		FormInstance template = templateCache.get(Integer.valueOf(formID));
		if (template == null) {
			template = CompactInstanceWrapper.loadTemplateInstance(formID);
			if (template == null) {
				throw new RuntimeException("no formdef found for form id [" + formID + "]");
			}
			templateCache.put(Integer.valueOf(formID), template);
		}
		return template;
	}

}
