package org.javarosa.formmanager.api;

import org.javarosa.core.model.FormDef;
import org.javarosa.form.api.FormEntryModel;

public class JrFormEntryModel extends FormEntryModel {

	private boolean readOnlyMode;
	
	public JrFormEntryModel (FormDef f) {
		this(f, false);
	}
	
	public JrFormEntryModel(FormDef f, boolean readOnlyMode) {
		this(f, readOnlyMode, FormEntryModel.REPEAT_STRUCTURE_NON_LINEAR);
	}
	
	public JrFormEntryModel (FormDef f, boolean readOnlyMode, int repeatStructure) {
		super(f, repeatStructure);
		this.readOnlyMode = readOnlyMode;
	}
	
	public boolean isReadOnlyMode () {
		return readOnlyMode;
	}
	
}
