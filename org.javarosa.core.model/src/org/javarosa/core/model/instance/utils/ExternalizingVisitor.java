package org.javarosa.core.model.instance.utils;

import java.io.DataOutputStream;
import java.io.IOException;

import org.javarosa.core.model.IFormDataModel;
import org.javarosa.core.model.instance.DataModelTree;
import org.javarosa.core.model.instance.QuestionDataElement;
import org.javarosa.core.model.instance.QuestionDataGroup;
import org.javarosa.core.model.instance.TreeElement;
import org.javarosa.core.util.externalizable.ExtUtil;
import org.javarosa.core.util.externalizable.ExtWrapTagged;
import org.javarosa.core.util.externalizable.Externalizable;

/**
 * The Externalizing Visitor walks a Data Model Tree and 
 * writes its elements serially to the output stream 
 * provided.
 * 
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
		//Don't actually do this. The DataModelTree is the IFormDataModel implementor, which means that it
		//is having externalize called on it already.
		//externalizeElement(tree);
	}

	/* (non-Javadoc)
	 * @see org.javarosa.core.model.utils.ITreeVisitor#visit(org.javarosa.core.model.TreeElement)
	 */
	public void visit(TreeElement element) {
		//Shouldn't happen
		System.out.println("bad news bears");
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
				ExtUtil.write(outStream, new ExtWrapTagged(ext));
			} catch (IOException e) {
				// #if debug.output == exception || debug.output == verbose
				e.printStackTrace();
				// #endif
				failure = true;
			}
		}
	}
}
