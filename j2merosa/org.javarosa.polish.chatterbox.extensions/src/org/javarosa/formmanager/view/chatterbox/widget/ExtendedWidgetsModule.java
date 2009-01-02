package org.javarosa.formmanager.view.chatterbox.widget;

import org.javarosa.core.Context;
import org.javarosa.core.api.IModule;
import org.javarosa.formmanager.view.chatterbox.widget.chart.WHOWeightTemplate;
import org.javarosa.model.GraphDataGroup;
import org.javarosa.xform.parse.GraphElementHandler;
import org.javarosa.xform.parse.XFormParser;

public class ExtendedWidgetsModule implements IModule {
	
	public void registerModule(Context context) {
		
		GraphElementHandler graphHandler = new GraphElementHandler();
		graphHandler.registerGraphType(WHOWeightTemplate.WHO_WEIGHT_TEMPLATE_NAME);
		XFormParser.registerHandler("graph", graphHandler);
		XFormParser.addDataType("jr:recordset", GraphDataGroup.GRAPH_DATA_ID);
		XFormParser.addModelPrototype(GraphDataGroup.GRAPH_DATA_ID, new GraphDataGroup());		
		
		XFormParser.registerControlType("table", ImmunizationWidget.CONTROL_IMMUNIZATION);
		//XFormParser.addDataType("jr:vaccinationdata", Immunization.)
	}

}