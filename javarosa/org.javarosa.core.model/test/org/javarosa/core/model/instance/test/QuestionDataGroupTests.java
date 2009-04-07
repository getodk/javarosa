/*
 * Copyright (C) 2009 JavaRosa-Core Project
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

package org.javarosa.core.model.instance.test;

import j2meunit.framework.Test;
import j2meunit.framework.TestCase;
import j2meunit.framework.TestMethod;
import j2meunit.framework.TestSuite;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import org.javarosa.core.model.IDataReference;
import org.javarosa.core.model.IFormDataModel;
import org.javarosa.core.model.data.IntegerData;
import org.javarosa.core.model.data.StringData;
import org.javarosa.core.model.instance.DataModelTree;
import org.javarosa.core.model.instance.TreeElement;
import org.javarosa.core.model.instance.utils.ITreeVisitor;
import org.javarosa.core.util.externalizable.DeserializationException;
import org.javarosa.core.util.externalizable.PrototypeFactory;

public class QuestionDataGroupTests extends TestCase {
	private final String stringElementName = "String Data Element";
	private final String groupName = "TestGroup";
	
	StringData stringData;
	IntegerData integerData;
	
	IDataReference stringReference; 
	
	IDataReference integerReference;
	
	TreeElement stringElement;
	TreeElement intElement;
	
	TreeElement group;
	
	private static int NUM_TESTS = 9;
	
	/* (non-Javadoc)
	 * @see j2meunit.framework.TestCase#setUp()
	 */
	protected void setUp() throws Exception {
		super.setUp();
		stringData = new StringData("Answer Value");
		integerData = new IntegerData(4);
		
		stringReference = new IDataReference() {
			String reference = "stringValue";
			
			public Object getReference() {
				return reference;
			}
			
			public void setReference(Object reference) {
				this.reference = (String)reference;
			}
			
			public boolean referenceMatches(IDataReference reference) {
				return this.reference.equals(reference.getReference());
			}
		
			
			public IDataReference clone()  {
				IDataReference newReference = null;
				try {
					newReference = (IDataReference)this.getClass().newInstance();
				} catch (InstantiationException e) {
					e.printStackTrace();
				} catch (IllegalAccessException e) {
					e.printStackTrace();
				}
				newReference.setReference(reference);
				return newReference;
			}
			public void readExternal(DataInputStream in, PrototypeFactory pf) throws IOException, DeserializationException {}

			public void writeExternal(DataOutputStream out) throws IOException {};

		};
		
		integerReference = new IDataReference() {
			Integer intReference = new Integer(15);
			
			public Object getReference() {
				return intReference;
			}
			
			public void setReference(Object reference) {
				this.intReference = (Integer)reference;
			}
			
			public boolean referenceMatches(IDataReference reference) {
				return this.intReference.equals(reference.getReference());
			}
		
			
			public IDataReference clone()  {
				IDataReference newReference = null;
				try {
					newReference = (IDataReference)this.getClass().newInstance();
				} catch (InstantiationException e) {
					e.printStackTrace();
				} catch (IllegalAccessException e) {
					e.printStackTrace();
				}
				newReference.setReference(intReference);
				return newReference;
			}
			public void readExternal(DataInputStream in, PrototypeFactory pf) throws IOException, DeserializationException {}

			public void writeExternal(DataOutputStream out) throws IOException {};

		};
		
		intElement  = new TreeElement("intElement");
		intElement.setValue(integerData);
		
		stringElement = new TreeElement(stringElementName);
		stringElement.setValue(stringData);
		
		group = new TreeElement(groupName);
	}
	
	public QuestionDataGroupTests(String name, TestMethod rTestMethod) {
		super(name, rTestMethod);
	}

	public QuestionDataGroupTests(String name) {
		super(name);
	}

	public QuestionDataGroupTests() {
		super();
	}	

	public Test suite() {
		TestSuite aSuite = new TestSuite();

		for (int i = 1; i <= NUM_TESTS; i++) {
			final int testID = i;

			aSuite.addTest(new QuestionDataGroupTests("QuestionDataGroup Test " + i, new TestMethod() {
				public void run (TestCase tc) {
					((QuestionDataGroupTests)tc).testMaster(testID);
				}
			}));
		}

		return aSuite;
	}
	public void testMaster (int testID) {
		//System.out.println("running " + testID);
		
		switch (testID) {
			case 1: testIsLeaf(); break;
			case 2: testGetName(); break;
			case 3: testSetName(); break;
			case 4: testAcceptsVisitor(); break;
			case 5: testAddLeafChild(); break;
			case 6: testAddTreeChild(); break;
			case 7: testContains(); break;
			case 8: testSuperclassMethods(); break;
			
		}
	}

	public void testIsLeaf() {
		assertTrue("A Group with no children should report being a leaf", group.isLeaf());
		group.addChild(stringElement);
		assertTrue("A Group with children should not report being a leaf", !group.isLeaf());
	}
	
	public void testGetName() {
		String name = "TestGroup";
		assertEquals("Question Data Group did not properly get its name", group.getName(), name);
		group.addChild(stringElement);
		assertEquals("Question Data Group's name was changed improperly", group.getName(), name);
	}
	
	public void testSetName() {
		String name = "TestGroup";
		group = new TreeElement(name);
		String newName = "TestGroupNew";
		group.setName(newName);
		assertEquals("Question Data Group did not properly get its name", group.getName(), newName);
	}
	
	private class MutableBoolean {
		private boolean bool;
		
		public MutableBoolean(boolean bool) {
			this.bool = bool;
		}
		
		void setValue(boolean bool) {
			this.bool = bool;
		}
		
		boolean getValue() {
			return bool;
		}
	}
	
	public void testAcceptsVisitor() {
		final MutableBoolean visitorAccepted = new MutableBoolean(false);
		final MutableBoolean dispatchedWrong = new MutableBoolean(false);
		ITreeVisitor sampleVisitor = new ITreeVisitor() {
			
			public void visit(DataModelTree tree) {
				dispatchedWrong.setValue(true);

			}
			public void visit(TreeElement element) {
				visitorAccepted.setValue(true);
			}
			public void visit(IFormDataModel dataModel) {
				dispatchedWrong.setValue(true);

			}
		};
		
		stringElement.accept(sampleVisitor);
		assertTrue("The visitor's visit method was not called correctly by the QuestionDataElement",visitorAccepted.getValue());
		
		assertTrue("The visitor was dispatched incorrectly by the QuestionDataElement",!dispatchedWrong.getValue());
	}
	
	private void testSuperclassMethods() {
		//stringElement should not have a root at this point.
		
		//TODO: Implement tests for the 'attribute' system.
	}
	
	private void testAddLeafChild() {


		boolean threw = false;
		boolean added = false;
		try {
			group.addChild(stringElement);
			assertTrue("Added element was not in Question Data Group's children!",group.getChildren().contains(stringElement));
		} 
		catch(RuntimeException e) {
			if(!added) {
				fail("Group did not report success adding a valid child");
			}
		}
		
		added=false;
		threw = false;
		try {
			TreeElement leafGroup = new TreeElement("leaf group");
			added = true;
			group.getChildren().contains(leafGroup);
		}
		catch (RuntimeException e) {
			if(!added) {
				fail("Group did not report success adding a valid child");
			}
			threw = true;
			
		}
	}
	
	private void testAddTreeChild() {
		TreeElement subTree = new TreeElement("subtree");
		TreeElement firstRootTree = new TreeElement("firstRoot");
		TreeElement secondRootTree = new TreeElement("secondRoot");
		
		TreeElement subElement = new TreeElement("SubElement");
		subElement.addChild(stringElement);
		subElement.addChild(intElement);
		
		//assertTrue("Group did not add valid subtree group as a child",subTree.addChild(subElement));
		//assertTrue("Group does not properly contain subtree group as a child",subTree.contains(subElement));
		
		//Looks like these are not valid anymore after the last round of changes.
		
	}
	
	private void testContains() {
		
	}
}
