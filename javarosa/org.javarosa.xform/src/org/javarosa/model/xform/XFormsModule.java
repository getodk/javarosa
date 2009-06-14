package org.javarosa.model.xform;

import java.io.IOException;

import org.javarosa.core.Context;
import org.javarosa.core.JavaRosaServiceProvider;
import org.javarosa.core.api.IModule;
import org.javarosa.core.model.FormDef;
import org.javarosa.core.model.data.IAnswerData;
import org.javarosa.core.model.instance.DataModelTree;
import org.javarosa.core.model.instance.TreeReference;
import org.javarosa.core.model.util.restorable.IXFormyFactory;
import org.javarosa.core.model.util.restorable.RestoreUtils;
import org.javarosa.core.services.transport.IDataPayload;
import org.javarosa.xform.parse.XFormParser;
import org.javarosa.xform.util.XFormAnswerDataParser;
import org.javarosa.xpath.XPathParseTool;

public class XFormsModule implements IModule {

	public void registerModule(Context context) {
		String[] classes = {
				"org.javarosa.model.xform.XPathReference",
				"org.javarosa.xpath.XPathConditional"
		};
		
		JavaRosaServiceProvider.instance().registerPrototypes(classes);
		JavaRosaServiceProvider.instance().registerPrototypes(XPathParseTool.xpathClasses);
		RestoreUtils.xfFact = new IXFormyFactory () {
			public TreeReference ref (String refStr) {
				return DataModelTree.unpackReference(new XPathReference(refStr));
			}
			
			public IDataPayload serializeModel (DataModelTree dm) {
				try {
					return (new XFormSerializingVisitor()).createSerializedPayload(dm);
				} catch (IOException e) {
					return null;
				}
			}

			public DataModelTree parseRestore(byte[] data, Class restorableType) {
				return XFormParser.restoreDataModel(data, restorableType);
			}
			
			public IAnswerData parseData (String textVal, int dataType, TreeReference ref, FormDef f) {
				return XFormAnswerDataParser.getAnswerData(textVal, dataType, XFormParser.ghettoGetQuestionDef(dataType, f, ref));
			}
		};
	}

}
