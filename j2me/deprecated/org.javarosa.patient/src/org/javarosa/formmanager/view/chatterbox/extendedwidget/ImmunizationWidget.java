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

package org.javarosa.formmanager.view.chatterbox.extendedwidget;

import org.javarosa.core.model.QuestionDef;
import org.javarosa.core.model.data.IAnswerData;
import org.javarosa.form.api.FormEntryCaption;
import org.javarosa.form.api.FormEntryPrompt;
import org.javarosa.formmanager.view.chatterbox.extendedwidget.table.VaccinationTable;
import org.javarosa.formmanager.view.chatterbox.widget.ChatterboxWidget;
import org.javarosa.formmanager.view.chatterbox.widget.ExpandedWidget;
import org.javarosa.patient.model.data.ImmunizationAnswerData;
import org.javarosa.patient.model.data.ImmunizationData;

import de.enough.polish.ui.Container;
import de.enough.polish.ui.Item;

public class ImmunizationWidget extends ExpandedWidget {
	
	public final static int CONTROL_IMMUNIZATION = 10;
	
	VaccinationTable table;
	
	Container container;
	
	ImmunizationData d;
	
	public ImmunizationWidget() {
		container = new Container(false);
		table = new VaccinationTable(false);
		container.add(table);
	}

	protected Item getEntryWidget(FormEntryPrompt prompt) {
		return container;
	}

	protected IAnswerData getWidgetValue() {
		d = table.getData();
		return new ImmunizationAnswerData(d);
	}

	protected void setWidgetValue(Object o) {
		if(o instanceof ImmunizationData ) {
			d = (ImmunizationData)o;
			table.setData(d);
		}
	}

	protected void updateWidget(FormEntryPrompt prompt) {
	}
	

	public int getNextMode () {
		return ChatterboxWidget.NEXT_ON_MANUAL;
	}

	public int widgetType() {
		return CONTROL_IMMUNIZATION;
	}

}
