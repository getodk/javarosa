package org.javarosa.core.model.utils;

import java.io.DataOutputStream;
import java.io.IOException;

import org.javarosa.core.model.IAnswerDataSerializer;
import org.javarosa.core.model.IFormDataModel;

public interface IDataModelSerializingVisitor extends IDataModelVisitor {
	
	byte[] serializeDataModel(IFormDataModel model) throws IOException;
	
	void setAnswerDataSerializer(IAnswerDataSerializer ads);

}
