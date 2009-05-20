/**
 * 
 */
package org.javarosa.cases.util;

import org.javarosa.cases.model.Case;
import org.javarosa.cases.storage.CaseRmsUtility;
import org.javarosa.core.JavaRosaServiceProvider;
import org.javarosa.core.services.storage.utilities.IRecordStoreEnumeration;

/**
 * @author Clayton Sims
 * @date May 18, 2009 
 *
 */
public class CaseRestorableTest {
	public static void test() {
		CaseRmsUtility utility = (CaseRmsUtility)JavaRosaServiceProvider.instance().getStorageManager().getRMSStorageProvider().getUtility(CaseRmsUtility.getUtilityName());
		for (IRecordStoreEnumeration en = utility.enumerateMetaData(); en.hasNextElement(); ) {
			Case c = new Case();
			try {
				utility.retrieveFromRMS(en.nextRecordId(), c);
				RestorableTestHarness.evaluateRestorable(c);
			} catch (Exception e) {
				e.printStackTrace();
				throw new RuntimeException("Case was busted!");
			}
		}
	}
}
