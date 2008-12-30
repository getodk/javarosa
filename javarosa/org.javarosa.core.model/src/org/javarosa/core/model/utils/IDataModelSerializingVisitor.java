package org.javarosa.core.model.utils;

import java.io.IOException;

import org.javarosa.core.model.FormDef;
import org.javarosa.core.model.IAnswerDataSerializer;
import org.javarosa.core.model.IFormDataModel;
import org.javarosa.core.services.transport.IDataPayload;

/**
 * An IDataModelSerializingVisitor serializes a DataModel
 * 
 * @author Clayton Sims
 *
 */
public interface IDataModelSerializingVisitor extends IDataModelVisitor {
	
	byte[] serializeDataModel(IFormDataModel model, FormDef formDef) throws IOException;
	
	byte[] serializeDataModel(IFormDataModel model) throws IOException;
	
	public IDataPayload createSerializedPayload	(IFormDataModel model) throws IOException;
	
	void setAnswerDataSerializer(IAnswerDataSerializer ads);

}
