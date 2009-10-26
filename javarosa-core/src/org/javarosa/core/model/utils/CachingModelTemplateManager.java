package org.javarosa.core.model.utils;

import java.util.Hashtable;
import java.util.Vector;

import org.javarosa.core.model.instance.DataModelTree;

public class CachingModelTemplateManager implements DataModelTemplateManager {

	private Hashtable<Integer, DataModelTree> templateCache;
	private Vector<Integer> allowedFormTypes;
	private boolean restrictFormTypes;
	
	public CachingModelTemplateManager () {
		this(true);
	}
	
	public CachingModelTemplateManager (boolean restrictFormTypes) {
		this.templateCache = new Hashtable<Integer, DataModelTree>();
		this.restrictFormTypes = restrictFormTypes;
		this.allowedFormTypes = new Vector<Integer>();
	}
	
	public void clearCache () {
		templateCache.clear();
	}
	
	public void addFormType (int formID) {
		if (!allowedFormTypes.contains(new Integer(formID))) {
			allowedFormTypes.add(new Integer(formID));
		}
	}
	
	public void resetFormTypes () {
		allowedFormTypes.clear();
	}
	
	public DataModelTree getTemplateModel (int formID) {
		if (restrictFormTypes && !allowedFormTypes.contains(new Integer(formID))) {
			throw new RuntimeException ("form ID [" + formID + "] is not an allowed form type!");
		}
		
		DataModelTree template = templateCache.get(new Integer(formID));
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
