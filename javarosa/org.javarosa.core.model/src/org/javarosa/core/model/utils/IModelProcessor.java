/**
 * 
 */
package org.javarosa.core.model.utils;

import org.javarosa.core.Context;
import org.javarosa.core.model.instance.DataModelTree;

/**
 * An interface for classes which are capable of parsing and performing actions
 * on Data Model objects.
 * 
 * @author Clayton Sims
 * @date Jan 27, 2009 
 *
 */
public interface IModelProcessor {
	
	/**
	 * Processes the provided data model.
	 * 
	 * @param tree The data model that will be handled.
	 */
	public void processModel(DataModelTree tree);
	
	/**
	 * Initializes this model processor with the provided
	 * context.
	 * 
	 * @param context An activity context
	 */
	public void initializeContext(Context context);
	
	/**
	 * Loads the provided context model with any information
	 * obtained from processing the models provided to this
	 * model processor.
	 * 
	 * @param context An activity context
	 */
	public void loadProcessedContext(Context context);
}
