package org.javarosa.core.model.utils;

import org.javarosa.core.model.IFormDataModel;

/**
 * An IDataModelVisitor visits every element in a DataModel
 * following the visitor design pattern.
 * 
 * @author Clayton Sims
 *
 */
public interface IDataModelVisitor {
	/**
	 * Performs any necessary operations on the IFormDataModel without
	 * visiting any of the Model's potential children.
	 * @param dataModel
	 */
	void visit(IFormDataModel dataModel);
}
