package org.javarosa.formmanager.api;

import org.javarosa.core.model.FormDef;
import org.javarosa.form.api.FormEntryModel;

public class JrFormEntryModel extends FormEntryModel {

	private boolean readOnlyMode;
	
	public JrFormEntryModel (FormDef f) {
		this(f, false);
	}
	
	public JrFormEntryModel (FormDef f, boolean readOnlyMode) {
		super(f);
		this.readOnlyMode = readOnlyMode;
	}
	
	public boolean isReadOnlyMode () {
		return readOnlyMode;
	}
	
}
