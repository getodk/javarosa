package org.javarosa.core.model.utils;

import java.io.DataOutputStream;

import org.javarosa.core.model.IAnswerDataSerializer;
import org.javarosa.core.model.IFormDataModel;

public interface IDataModelSerializingVisitor extends IDataModelVisitor {
	
	DataOutputStream serializeDataModel(IFormDataModel model);
	
	void setAnswerDataSerializer(IAnswerDataSerializer ads);

}
