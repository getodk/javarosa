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

/**
 * 
 */
package org.javarosa.patient.util;

import org.javarosa.core.data.IDataPointer;
import org.javarosa.core.model.FormDef;
import org.javarosa.core.model.IAnswerDataSerializer;
import org.javarosa.core.model.data.IAnswerData;
import org.javarosa.core.model.instance.TreeElement;
import org.javarosa.patient.model.data.ImmunizationAnswerData;

/**
 * @author Clayton Sims
 *
 */
public class ImmunizationAnswerDataSerializer implements IAnswerDataSerializer {

	public boolean canSerialize(IAnswerData data) {
		if(data instanceof ImmunizationAnswerData) {
			return true;
		}
		return false;
	
	}
	public boolean canSerialize(TreeElement element) {
		if(element.getValue() instanceof ImmunizationAnswerData) {
			return true;
		}
		return false;
	}

	/* (non-Javadoc)
	 * @see org.javarosa.core.model.IAnswerDataSerializer#registerAnswerSerializer(org.javarosa.core.model.IAnswerDataSerializer)
	 */
	public void registerAnswerSerializer(IAnswerDataSerializer ads) {
		//We don't handle this
	}

	/* (non-Javadoc)
	 * @see org.javarosa.core.model.IAnswerDataSerializer#serializeAnswerData(org.javarosa.core.model.data.IAnswerData)
	 */
	public Object serializeAnswerData(TreeElement element,
			FormDef schema) {
		return "Immunization Data Serializer is not done yet";
	}

	public Object serializeAnswerData(IAnswerData data) {
		return "Immunization Data Serializer is not done yet";
	}

	public Object serializeAnswerData(IAnswerData data, int dataType) {
		return "Immunization Data Serializer is not done yet";
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.javarosa.core.model.IAnswerDataSerializer#containsExternalData(org.javarosa.core.model.data.IAnswerData)
	 */
	public Boolean containsExternalData(IAnswerData data) {
		return null;
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.javarosa.core.model.IAnswerDataSerializer#retrieveExternalDataPointer(org.javarosa.core.model.data.IAnswerData)
	 */
	public IDataPointer[] retrieveExternalDataPointer(IAnswerData data) {
		return null;
	}
}
