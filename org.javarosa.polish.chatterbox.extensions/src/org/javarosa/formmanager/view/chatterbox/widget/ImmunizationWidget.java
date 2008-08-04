package org.javarosa.formmanager.view.chatterbox.widget;

import org.javarosa.core.model.QuestionDef;
import org.javarosa.core.model.data.IAnswerData;
import org.javarosa.formmanager.view.chatterbox.widget.table.Table;
import org.javarosa.patient.model.data.ImmunizationData;

import de.enough.polish.ui.Item;

public class ImmunizationWidget extends ExpandedWidget {
	
	public final static int CONTROL_IMMUNIZATION = 10;
	
	Table table;
	
	ImmunizationData d;
	
	public ImmunizationWidget() {
		table = new Table("");
		//TODO: Set table values
	}

	protected Item getEntryWidget(QuestionDef question) {
		return table;
	}

	protected IAnswerData getWidgetValue() {
		return d;
	}

	protected void setWidgetValue(Object o) {
		if(o instanceof ImmunizationData ) {
			d = (ImmunizationData)o;
			//TODO: Update table values
		}
	}

	protected void updateWidget(QuestionDef question) {
	}
	

	public int getNextMode () {
		return ChatterboxWidget.NEXT_ON_MANUAL;
	}

	public int widgetType() {
		return CONTROL_IMMUNIZATION;
	}

}
