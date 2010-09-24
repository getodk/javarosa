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

package org.javarosa.demo.xform.test;

import j2meunit.framework.Test;
import j2meunit.framework.TestCase;
import j2meunit.framework.TestMethod;
import j2meunit.framework.TestSuite;

import org.javarosa.core.model.FormDef;
import org.javarosa.xform.util.XFormUtils;

public class DemoFormsTest extends TestCase  {
		
		public DemoFormsTest(String name, TestMethod rTestMethod) {
			super(name, rTestMethod);
		}
		
		public DemoFormsTest(String name) {
			super(name);
		}
		
		public DemoFormsTest() {
			super();
		}	
		
		public Test suite() {
			TestSuite aSuite = new TestSuite();
			aSuite.addTest((new DemoFormsTest("testForms", new TestMethod() {
				public void run(TestCase tc) { ((DemoFormsTest)tc).testBasicParsing(); }
			})));
					
			return aSuite;
		}
		
		public void testForm(String formName) {
			FormDef def = null;
			try{
				def = XFormUtils.getFormFromResource(formName);
			}
			catch(Exception e) {
				fail("Form Parse Exceptions for form " + formName + " : " + e.getMessage() );
			}
			if(def==null) {
				fail("Form Parse Failed for form " + formName);
			}
		}
		
		public void testBasicParsing() {
			testForm("/hmis-a_draft.xhtml");
			testForm("/hmis-b_draft.xhtml");
			testForm("/shortform.xhtml");
			testForm("/CHMTTL.xhtml");
			testForm("/condtest.xhtml");
		}
}
