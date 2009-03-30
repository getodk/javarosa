package org.javarosa.core.model.util.restorable;

import org.javarosa.core.model.instance.DataModelTree;
import org.javarosa.core.model.instance.TreeReference;

public interface Restorable {

	String getRestorableType ();
	DataModelTree exportData ();
	void templateData (DataModelTree dm, TreeReference parentRef);
	void importData (DataModelTree dm);
	
}
