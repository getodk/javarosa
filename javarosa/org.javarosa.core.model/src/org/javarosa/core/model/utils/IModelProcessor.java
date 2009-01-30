/**
 * 
 */
package org.javarosa.core.model.utils;

import org.javarosa.core.Context;
import org.javarosa.core.model.instance.DataModelTree;

/**
 * @author Clayton Sims
 * @date Jan 27, 2009 
 *
 */
public interface IModelProcessor {
	public void processModel(DataModelTree tree);
	
	public void initializeContext(Context context);
	
	public void loadProcessedContext(Context context);
}
