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

package org.javarosa.core.model.instance.test;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import org.javarosa.core.model.IDataReference;
import org.javarosa.core.model.data.IAnswerData;
import org.javarosa.core.model.data.IntegerData;
import org.javarosa.core.model.data.StringData;
import org.javarosa.core.model.instance.AbstractTreeElement;
import org.javarosa.core.model.instance.FormInstance;
import org.javarosa.core.model.instance.TreeElement;
import org.javarosa.core.model.instance.utils.ITreeVisitor;
import org.javarosa.core.util.externalizable.DeserializationException;
import org.javarosa.core.util.externalizable.PrototypeFactory;

public class QuestionDataElementTests extends TestCase{
	private final String stringElementName = "String Data Element";

	StringData stringData;
	IntegerData integerData;

	IDataReference stringReference;

	IDataReference integerReference;

	TreeElement stringElement;
	TreeElement intElement;

	private static int NUM_TESTS = 7;

	public void setUp() throws Exception {
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
			/*
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
			*/
			public void readExternal(DataInputStream in, PrototypeFactory pf) throws IOException, DeserializationException {}

			public void writeExternal(DataOutputStream out) throws IOException {}

		};

		integerReference = new IDataReference() {
			Integer intReference = new Integer(15);

			public Object getReference() {
				return intReference;
			}

			public void setReference(Object reference) {
				this.intReference = (Integer)reference;
			}
			/*
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
			*/
			public void readExternal(DataInputStream in, PrototypeFactory pf) throws IOException, DeserializationException {}

			public void writeExternal(DataOutputStream out) throws IOException {}

		};

		intElement  = new TreeElement("intElement");
		intElement.setValue(integerData);

		stringElement = new TreeElement(stringElementName);
		stringElement.setValue(stringData);

	}

	public QuestionDataElementTests(String name) {
		super(name);
		System.out.println("Running " + this.getClass().getName() + " test: " + name + "...");
	}

	public static Test suite() {
		TestSuite suite = new TestSuite();

		for (int i = 1; i <= NUM_TESTS; i++) {
			final int testID = i;

			suite.addTest(new QuestionDataElementTests(testMaster(testID)));
		}

		return suite;
	}

	public static String testMaster (int testID) {
		//System.out.println("running " + testID);

		switch (testID) {
			case 1: return "testIsLeaf";
			case 2: return "testGetName";
			case 3: return "testSetName";
			case 4: return "testGetValue";
			case 5: return "testSetValue";
			case 6: return "testAcceptsVisitor";
			case 7: return "testSuperclassMethods";

		}
		throw new IllegalStateException("Unexpected index");
	}

	public void testIsLeaf() {
		assertTrue("Question Data Element returned negative for being a leaf",stringElement.isLeaf());
	}

	public void testGetName() {
		assertEquals("Question Data Element 'string' did not properly get its name", stringElement.getName(), stringElementName);
	}

	public void testSetName() {
		String newName = new String("New Name");
		stringElement.setName(newName);

		assertEquals("Question Data Element 'string' did not properly set its name", stringElement.getName(), newName);
	}

	public void testGetValue() {
		IAnswerData data = stringElement.getValue();
		assertEquals("Question Data Element did not return the correct value",data,stringData);
	}

	public void testSetValue() {
		stringElement.setValue(integerData);
		assertEquals("Question Data Element did not set value correctly",stringElement.getValue(),integerData);

		try {
			stringElement.setValue(null);
		} catch(Exception e) {
			fail("Question Data Element did not allow for its value to be set as null");
		}

		assertEquals("Question Data Element did not return a null value correctly", stringElement.getValue(),null);

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

			public void visit(FormInstance tree) {
				dispatchedWrong.bool = true;
			}

			public void visit(AbstractTreeElement element) {
				visitorAccepted.bool = true;
			}
		};

		stringElement.accept(sampleVisitor);
		assertTrue("The visitor's visit method was not called correctly by the QuestionDataElement",visitorAccepted.getValue());

		assertTrue("The visitor was dispatched incorrectly by the QuestionDataElement",!dispatchedWrong.getValue());
	}

	public void testSuperclassMethods() {
		//stringElement should not have a root at this point.

		//TODO: Implement tests for the 'attribute' system.
	}
}
