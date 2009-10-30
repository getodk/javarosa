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

package org.javarosa.referral.activity;

import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Displayable;

import org.javarosa.core.api.Constants;
import org.javarosa.core.api.State;
import org.javarosa.core.model.FormDef;
import org.javarosa.core.model.instance.DataModelTree;
import org.javarosa.core.services.storage.IStorageIterator;
import org.javarosa.core.services.storage.IStorageUtility;
import org.javarosa.core.services.storage.StorageManager;
import org.javarosa.core.util.TrivialTransitions;
import org.javarosa.j2me.view.J2MEDisplay;
import org.javarosa.referral.model.Referrals;
import org.javarosa.referral.view.ReportView;
import org.javarosa.xform.util.XFormAnswerDataSerializer;

public abstract class ReferralReport implements TrivialTransitions, State, CommandListener {

	private Referrals referrals;
	private DataModelTree model;
	private ReportView view;

	public ReferralReport (String formName, int modelID, int formID) {
		IStorageUtility referrals = StorageManager.getStorage(Referrals.STORAGE_KEY);
		IStorageUtility models = StorageManager.getStorage(DataModelTree.STORAGE_KEY);
		IStorageUtility forms = StorageManager.getStorage(FormDef.STORAGE_KEY);
		
		IStorageIterator ri = referrals.iterate();
		this.referrals = null;
		boolean found = false;
		while (ri.hasMore()) {
			this.referrals = (Referrals)ri.nextRecord();
			if (formName.equals(this.referrals.getFormName())) {
				found = true;
				break;
			}
		}
		if (!found)
			this.referrals = null;
		
		if(this.referrals == null) {
			this.referrals = new Referrals();
		} else {
			FormDef temp = (FormDef)forms.read(formID);			
			model = (DataModelTree)models.read(modelID);
			temp.setDataModel(model);
			temp.initialize(false);
		}
		
	}
	
	public void start() {
		view = new ReportView("Referral Report");
		view.setReferrals(referrals.getPositiveReferrals(model, new XFormAnswerDataSerializer()));
		view.setCommandListener(this);
		
		J2MEDisplay.setView(view);
	}

	public void commandAction(Command arg0, Displayable arg1) {
		done();
	}
	

}