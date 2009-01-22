package org.javarosa.formmanager.view.chatterbox.widget;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import org.javarosa.core.Context;
import org.javarosa.core.JavaRosaServiceProvider;
import org.javarosa.core.api.IModule;
import org.javarosa.core.util.externalizable.DeserializationException;
import org.javarosa.core.util.externalizable.ExtUtil;
import org.javarosa.core.util.externalizable.ExtWrapTagged;
import org.javarosa.core.util.externalizable.PrototypeFactory;
import org.javarosa.formmanager.view.chatterbox.widget.chart.WHOWeightTemplate;
import org.javarosa.model.GraphDataGroup;
import org.javarosa.xform.parse.GraphElementHandler;
import org.javarosa.xform.parse.XFormParser;

public class ExtendedWidgetsModule implements IModule {
	
	public void registerModule(Context context) {
		
		GraphElementHandler graphHandler = new GraphElementHandler();
		graphHandler.registerGraphType(WHOWeightTemplate.WHO_WEIGHT_TEMPLATE_NAME);
		XFormParser.registerHandler("graph", graphHandler);
		XFormParser.addDataType("recordset", GraphDataGroup.GRAPH_DATA_ID);
		XFormParser.addModelPrototype(GraphDataGroup.GRAPH_DATA_ID, new GraphDataGroup());		
		
		XFormParser.registerControlType("table", ImmunizationWidget.CONTROL_IMMUNIZATION);
		//XFormParser.addDataType("jr:vaccinationdata", Immunization.)
		
		String[] classes = {
				"org.javarosa.model.GraphDataGroup",
		};
		
		JavaRosaServiceProvider.instance().registerPrototypes(classes);
		ByteArrayOutputStream bis = new ByteArrayOutputStream();
		DataOutputStream out = new DataOutputStream(bis);
		try {
			ExtUtil.write(out, new ExtWrapTagged(new GraphDataGroup()));
			byte[] test = bis.toByteArray();
			DataInputStream in = new DataInputStream(new ByteArrayInputStream(test));
			ExtUtil.read(in, new ExtWrapTagged(), new PrototypeFactory(JavaRosaServiceProvider.instance().getPrototypes()));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			throw new RuntimeException("FUCK");
		} catch (DeserializationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			throw new RuntimeException("FUCK2");
		}
		
	}

}