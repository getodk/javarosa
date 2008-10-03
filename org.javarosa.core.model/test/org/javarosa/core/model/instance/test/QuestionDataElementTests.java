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
import org.javarosa.core.model.data.IAnswerData;
import org.javarosa.core.model.data.IntegerData;
import org.javarosa.core.model.data.StringData;
import org.javarosa.core.model.instance.DataModelTree;
import org.javarosa.core.model.instance.QuestionDataElement;
import org.javarosa.core.model.instance.QuestionDataGroup;
import org.javarosa.core.model.instance.TreeElement;
import org.javarosa.core.model.instance.utils.ITreeVisitor;
import org.javarosa.core.util.externalizable.PrototypeFactory;
import org.javarosa.core.util.externalizable.PrototypeFactoryDeprecated;
import org.javarosa.core.util.externalizable.DeserializationException;

public class QuestionDataElementTests extends TestCase{
	private final String stringElementName = "String Data Element";
	
	StringData stringData;
	IntegerData integerData;
	
	IDataReference stringReference; 
	
	IDataReference integerReference;
	
	QuestionDataElement stringElement;
	QuestionDataElement intElement;
	
	private static int NUM_TESTS = 8;
	
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

			public void writeExternal(DataOutputStream out) throws IOException {}

		};
		
		intElement  = new QuestionDataElement("intElement", integerReference);
		intElement.setValue(integerData);
		
		stringElement = new QuestionDataElement(stringElementName, stringReference);
		stringElement.setValue(stringData);
		
	}
	
	public QuestionDataElementTests(String name, TestMethod rTestMethod) {
		super(name, rTestMethod);
	}

	public QuestionDataElementTests(String name) {
		super(name);
	}

	public QuestionDataElementTests() {
		super();
	}	

	public Test suite() {
		TestSuite aSuite = new TestSuite();

		for (int i = 1; i <= NUM_TESTS; i++) {
			final int testID = i;

			aSuite.addTest(new QuestionDataElementTests("QuestionDataElement Test " + i, new TestMethod() {
				public void run (TestCase tc) {
					((QuestionDataElementTests)tc).testMaster(testID);
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
			case 4: testGetValue(); break;
			case 5: testSetValue(); break;
			case 6: testReferenceMatch(); break;
			case 7: testAcceptsVisitor(); break;
			case 8: testSuperclassMethods(); break;
			
		}
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
	
	public void testReferenceMatch() {
		assertTrue("String Element's reference did not match the correct reference", stringElement.matchesReference(stringReference));
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
				
			}
			public void visit(TreeElement element) {
				dispatchedWrong.setValue(true);
			}
			public void visit(QuestionDataElement element) {
				visitorAccepted.setValue(true);
			}
			public void visit(QuestionDataGroup element) {
				
			}
			public void visit(IFormDataModel dataModel) {
				
			}
		};
		
		stringElement.accept(sampleVisitor);
		assertTrue("The visitor's visit method was not called correctly by the QuestionDataElement",visitorAccepted.getValue());
		
		assertTrue("The visitor was dispatched incorrectly by the QuestionDataElement",!dispatchedWrong.getValue());
	}
	
	private void testSuperclassMethods() {
		//stringElement should not have a root at this point.
		PrototypeFactoryDeprecated factory = new PrototypeFactoryDeprecated();
		stringElement.setFactory(factory);
		assertEquals("The QuestionDataElement 'stringElement' is not properly registering or returning factories",stringElement.getFactory(), factory);
		
		//TODO: Implement tests for the 'attribute' system.
	}
}
