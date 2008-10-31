package org.javarosa.core.model.instance.utils;

import org.javarosa.core.model.instance.DataModelTree;
import org.javarosa.core.model.instance.QuestionDataElement;
import org.javarosa.core.model.instance.QuestionDataGroup;
import org.javarosa.core.model.instance.TreeElement;
import org.javarosa.core.model.utils.IDataModelVisitor;

/**
 * ITreeVisitor is a visitor interface for the elements of the 
 * DataModelTree tree elements. In the case of composite elements,
 * method dispatch for composite members occurs following dispatch
 * for the composing member.
 * 
 * @author Clayton Sims
 *
 */
public interface ITreeVisitor extends IDataModelVisitor {
	public void visit(DataModelTree tree);
	public void visit(TreeElement element);
	public void visit(QuestionDataElement element);
	public void visit(QuestionDataGroup element);
}
