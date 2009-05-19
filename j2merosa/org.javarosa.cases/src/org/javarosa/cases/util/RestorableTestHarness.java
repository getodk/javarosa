/**
 * 
 */
package org.javarosa.cases.util;

import org.javarosa.core.model.instance.DataModelTree;
import org.javarosa.core.model.util.restorable.Restorable;

/**
 * @author Clayton Sims
 * @date May 18, 2009 
 *
 */
public class RestorableTestHarness {
	public static boolean evaluateRestorable(Restorable r) {
		DataModelTree tree = r.exportData();
		r.importData(tree);
		
		//If exceptions were thrown, this'll fail.
		return true;
	}
}
