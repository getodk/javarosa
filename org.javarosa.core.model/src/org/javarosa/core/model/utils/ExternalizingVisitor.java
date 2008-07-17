package org.javarosa.core.model.utils;

import java.io.DataOutputStream;
import java.io.IOException;

import org.javarosa.core.model.IFormDataModel;
import org.javarosa.core.model.instance.DataModelTree;
import org.javarosa.core.model.instance.QuestionDataElement;
import org.javarosa.core.model.instance.QuestionDataGroup;
import org.javarosa.core.model.instance.TreeElement;
import org.javarosa.core.services.storage.utilities.Externalizable;

/**
 * @author Clayton Sims
 *
 */
public class ExternalizingVisitor implements ITreeVisitor {

	DataOutputStream outStream;
	
	boolean failure;
	
	public ExternalizingVisitor(DataOutputStream outputStream) {
		outStream = outputStream; 
	}
	
	/* (non-Javadoc)
	 * @see org.javarosa.core.model.utils.ITreeVisitor#visit(org.javarosa.core.model.DataModelTree)
	 */
	public void visit(DataModelTree tree) {
		externalizeElement(tree);
	}

	/* (non-Javadoc)
	 * @see org.javarosa.core.model.utils.ITreeVisitor#visit(org.javarosa.core.model.TreeElement)
	 */
	public void visit(TreeElement element) {
		//Shouldn't happen
	}

	/* (non-Javadoc)
	 * @see org.javarosa.core.model.utils.ITreeVisitor#visit(org.javarosa.core.model.QuestionDataElement)
	 */
	public void visit(QuestionDataElement element) {
		externalizeElement(element);
	}

	/* (non-Javadoc)
	 * @see org.javarosa.core.model.utils.ITreeVisitor#visit(org.javarosa.core.model.QuestionDataGroup)
	 */
	public void visit(QuestionDataGroup element) {
		externalizeElement(element);
	}

	/* (non-Javadoc)
	 * @see org.javarosa.core.model.utils.IDataModelVisitor#visit(org.javarosa.core.model.IFormDataModel)
	 */
	public void visit(IFormDataModel dataModel) {
		//Don't do anything. It was the root of externalization
	}

	private void externalizeElement(Externalizable ext) {
		if (!failure) {
			try {
				ext.writeExternal(outStream);
			} catch (IOException e) {
				// #if debug.output == exception || debug.output == verbose
				e.printStackTrace();
				// #endif
				failure = true;
			}
		}
	}
}
