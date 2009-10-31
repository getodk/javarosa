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

package org.javarosa.formmanager.view.chatterbox.widget;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import org.javarosa.core.api.IModule;
import org.javarosa.core.services.PrototypeManager;
import org.javarosa.core.util.externalizable.DeserializationException;
import org.javarosa.core.util.externalizable.ExtUtil;
import org.javarosa.core.util.externalizable.ExtWrapTagged;
import org.javarosa.core.util.externalizable.PrototypeFactory;
import org.javarosa.formmanager.view.chatterbox.widget.chart.WHOWeightTemplate;
import org.javarosa.model.GraphDataGroup;
import org.javarosa.xform.parse.GraphElementHandler;
import org.javarosa.xform.parse.XFormParser;

public class ExtendedWidgetsModule implements IModule {
	
	public void registerModule() {
		
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
		
		PrototypeManager.registerPrototypes(classes);
		ByteArrayOutputStream bis = new ByteArrayOutputStream();
		DataOutputStream out = new DataOutputStream(bis);
		try {
			ExtUtil.write(out, new ExtWrapTagged(new GraphDataGroup()));
			byte[] test = bis.toByteArray();
			DataInputStream in = new DataInputStream(new ByteArrayInputStream(test));
			ExtUtil.read(in, new ExtWrapTagged());
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