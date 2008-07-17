package org.javarosa.core.model.utils;

import org.javarosa.core.model.DataModelTree;
import org.javarosa.core.model.QuestionDataElement;
import org.javarosa.core.model.QuestionDataGroup;
import org.javarosa.core.model.TreeElement;

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
