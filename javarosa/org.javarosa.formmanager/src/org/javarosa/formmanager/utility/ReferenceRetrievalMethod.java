package org.javarosa.formmanager.utility;

import org.javarosa.core.Context;
import org.javarosa.core.model.FormDef;

public class ReferenceRetrievalMethod implements IFormDefRetrievalMethod {
	FormDef formDef;

	public FormDef retreiveFormDef(Context context) {
		return formDef;
	}

	/**
	 * @return the formDef
	 */
	public FormDef getFormDef() {
		return formDef;
	}

	/**
	 * @param formDef the formDef to set
	 */
	public void setFormDef(FormDef formDef) {
		this.formDef = formDef;
	}
}
