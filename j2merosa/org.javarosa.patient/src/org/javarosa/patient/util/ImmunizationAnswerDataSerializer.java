/**
 * 
 */
package org.javarosa.patient.util;

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
}
