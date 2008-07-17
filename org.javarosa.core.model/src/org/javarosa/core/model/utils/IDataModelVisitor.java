package org.javarosa.core.model.utils;

import org.javarosa.core.model.IFormDataModel;

public interface IDataModelVisitor {
	void visit(IFormDataModel dataModel);
}
