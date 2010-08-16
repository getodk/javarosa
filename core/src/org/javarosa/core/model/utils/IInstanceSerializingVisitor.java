/*
 * Copyright (C) 2009 JavaRosa
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package org.javarosa.core.model.utils;

import java.io.IOException;

import org.javarosa.core.model.FormDef;
import org.javarosa.core.model.IAnswerDataSerializer;
import org.javarosa.core.model.IDataReference;
import org.javarosa.core.model.instance.FormInstance;
import org.javarosa.core.services.transport.payload.IDataPayload;

/**
 * An IInstanceSerializingVisitor serializes a DataModel
 * 
 * @author Clayton Sims
 *
 */
public interface IInstanceSerializingVisitor extends IInstanceVisitor {
	
	//LEGACY: Should remove
	byte[] serializeInstance(FormInstance model, FormDef formDef) throws IOException;
	
	byte[] serializeInstance(FormInstance model, IDataReference ref) throws IOException;
	byte[] serializeInstance(FormInstance model) throws IOException;
	
	public IDataPayload createSerializedPayload	(FormInstance model, IDataReference ref) throws IOException;
	public IDataPayload createSerializedPayload	(FormInstance model) throws IOException;
	
	void setAnswerDataSerializer(IAnswerDataSerializer ads);
	
	public IInstanceSerializingVisitor newInstance();

}
