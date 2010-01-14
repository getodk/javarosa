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

package org.javarosa.formmanager.test;

import j2meunit.framework.Test;
import j2meunit.framework.TestCase;
import j2meunit.framework.TestMethod;
import j2meunit.framework.TestSuite;

import java.io.ByteArrayInputStream;

import javax.microedition.lcdui.StringItem;

import org.javarosa.core.Context;
import org.javarosa.core.model.FormDef;
import org.javarosa.core.model.FormIndex;
import org.javarosa.core.model.IFormElement;
import org.javarosa.core.model.QuestionDef;
import org.javarosa.formmanager.view.chatterbox.util.ChatterboxContext;
import org.javarosa.formmanager.view.chatterbox.widget.ChatterboxWidget;
import org.javarosa.formmanager.view.chatterbox.widget.ChatterboxWidgetFactory;
import org.javarosa.formmanager.view.chatterbox.widget.ExpandedWidget;
import org.javarosa.formmanager.view.chatterbox.widget.GraphWidget;
import org.javarosa.test.sampleforms.SampleForms;
import org.javarosa.xform.util.XFormUtils;

public class ExtensionIntegrationTests extends TestCase {

	byte[] immun_form;
	public ExtensionIntegrationTests(String name, TestMethod rTestMethod) {
		super(name, rTestMethod);
	}

	public ExtensionIntegrationTests(String name) {
		super(name);
	}
	
	public ExtensionIntegrationTests() {
		super();
	}	
	
	public Test suite() {
		TestSuite aSuite = new TestSuite();
		aSuite.addTest(new ExtensionIntegrationTests("Graph Widget Integration Test", new TestMethod() {
			public void run (TestCase tc) {
				((ExtensionIntegrationTests)tc).testGraphWidget();
			}
		}));
		return aSuite;
	}

	public void testWidget(ExpandedWidget widget, byte[] form) {
		try {
			FormDef def = XFormUtils
					.getFormFromInputStream(new ByteArrayInputStream(form));
			ChatterboxContext formEntryContext = new ChatterboxContext(
					new Context());
			formEntryContext.addCustomWidget(widget);

			ChatterboxWidgetFactory factory = new ChatterboxWidgetFactory(null);
			factory.registerExtendedWidget(widget.widgetType(), widget);

			FormIndex index = FormIndex.createBeginningOfFormIndex();
			index = def.incrementIndex(index);

			while (!index.isEndOfFormIndex()) {
				IFormElement element = def.getChild(index);
				if (element instanceof QuestionDef) {
					ChatterboxWidget cboxWidget = factory.getWidget(index,
							def, ChatterboxWidget.VIEW_NOT_SET);
					if (cboxWidget == null) {
						fail("There was no chatterbox widget available for the name "
								+ widget.getClass().getName());
					}
				}
				index = def.incrementIndex(index);
			}
		} catch (Exception e) {
			e.printStackTrace();
			fail("Exception while testing chatterbox widget integration for the class "
					+ widget.getClass().getName());
		}
	}

	public void testImmunizationWidget() {

	}

	public void testGraphWidget() {
		try {
			String classpath = System.getProperty("java.class.path");
			System.out.println(classpath);
			StringItem d = new StringItem("asfd","asfd");
			if (d == null) {
				//int a = 3;
			}
			GraphWidget graph = new GraphWidget();			
			testWidget(graph, SampleForms.getGraphWidgetForm());
			
		} catch(Exception e) {
			fail("Couldn't create Graph widget");
		}
	}
	
	
}
