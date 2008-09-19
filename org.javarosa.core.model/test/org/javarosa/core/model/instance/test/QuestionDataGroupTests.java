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
import org.javarosa.core.model.instance.QuestionDataElement;
import org.javarosa.core.model.instance.QuestionDataGroup;
import org.javarosa.core.model.instance.TreeElement;
import org.javarosa.core.model.instance.utils.ITreeVisitor;
import org.javarosa.core.model.utils.PrototypeFactory;
import org.javarosa.core.util.UnavailableExternalizerException;

public class QuestionDataGroupTests extends TestCase {
	private final String stringElementName = "String Data Element";
	private final String groupName = "TestGroup";
	
	StringData stringData;
	IntegerData integerData;
	
	IDataReference stringReference; 
	
	IDataReference integerReference;
	
	QuestionDataElement stringElement;
	QuestionDataElement intElement;
	
	QuestionDataGroup group;
	
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
			public void readExternal(DataInputStream in) throws IOException, InstantiationException, IllegalAccessException, UnavailableExternalizerException {}

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
			public void readExternal(DataInputStream in) throws IOException, InstantiationException, IllegalAccessException, UnavailableExternalizerException {}

			public void writeExternal(DataOutputStream out) throws IOException {};

		};
		
		intElement  = new QuestionDataElement("intElement", integerReference);
		intElement.setValue(integerData);
		
		stringElement = new QuestionDataElement(stringElementName, stringReference);
		stringElement.setValue(stringData);
		
		group = new QuestionDataGroup(groupName);
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
			case 4: testReferenceFailure(); break;
			case 5: testAcceptsVisitor(); break;
			case 6: testAddLeafChild(); break;
			case 7: testAddTreeChild(); break;
			case 8: testContains(); break;
			case 9: testSuperclassMethods(); break;
			
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
		group = new QuestionDataGroup(name);
		String newName = "TestGroupNew";
		group.setName(newName);
		assertEquals("Question Data Group did not properly get its name", group.getName(), newName);
	}
	
	public void testReferenceFailure() {
		group.setReference(stringReference);
		assertTrue("By default, Question Data Groups should not set references", !group.matchesReference(stringReference));
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
		PrototypeFactory factory = new PrototypeFactory();
		stringElement.setFactory(factory);
		assertEquals("The QuestionDataElement 'stringElement' is not properly registering or returning factories",stringElement.getFactory(), factory);
		
		//TODO: Implement tests for the 'attribute' system.
	}
	
	private void testAddLeafChild() {
		assertTrue("Group did not report success adding a valid child",group.addChild(stringElement));
		assertTrue("Added element was not in Question Data Group's children!",group.getChildren().contains(stringElement));
		
		assertTrue("Group improperty reported success adding an invalid child",!group.addChild(stringElement));
		
		QuestionDataGroup leafGroup = new QuestionDataGroup("leaf group");
		assertTrue("Group did not report success adding a valid child",group.addChild(leafGroup));
		assertTrue("Added element was not in Question Data Group's children!",group.getChildren().contains(leafGroup));
	}
	
	private void testAddTreeChild() {
		QuestionDataGroup subTree = new QuestionDataGroup("subtree");
		QuestionDataGroup firstRootTree = new QuestionDataGroup("firstRoot");
		QuestionDataGroup secondRootTree = new QuestionDataGroup("secondRoot");
		
		QuestionDataGroup subElement = new QuestionDataGroup("SubElement");
		subElement.addChild(stringElement);
		subElement.addChild(intElement);
		
		assertTrue("Group did not add valid subtree group as a child",subTree.addChild(subElement));
		assertTrue("Group does not properly contain subtree group as a child",subTree.contains(subElement));		
		
	}
	
	private void testContains() {
		
	}
}
