package org.javarosa.core.model.util.restorable;

import org.javarosa.core.model.FormDef;
import org.javarosa.core.model.data.IAnswerData;
import org.javarosa.core.model.instance.DataModelTree;
import org.javarosa.core.model.instance.TreeReference;
import org.javarosa.core.services.transport.IDataPayload;

public interface IXFormyFactory {
	TreeReference ref (String refStr);
	IDataPayload serializeModel (DataModelTree dm);
	DataModelTree parseRestore (byte[] data, Class restorableType);
	IAnswerData parseData (String textVal, int dataType, TreeReference ref, FormDef f);
}