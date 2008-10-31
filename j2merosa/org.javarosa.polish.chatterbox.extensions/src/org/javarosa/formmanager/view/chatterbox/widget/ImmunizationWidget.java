package org.javarosa.formmanager.view.chatterbox.widget;

import org.javarosa.core.model.QuestionDef;
import org.javarosa.core.model.data.IAnswerData;
import org.javarosa.formmanager.view.chatterbox.widget.table.Table;
import org.javarosa.formmanager.view.chatterbox.widget.table.VaccinationTable;
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

	protected Item getEntryWidget(QuestionDef question) {
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

	protected void updateWidget(QuestionDef question) {
	}
	

	public int getNextMode () {
		return ChatterboxWidget.NEXT_ON_MANUAL;
	}

	public int widgetType() {
		return CONTROL_IMMUNIZATION;
	}

}
