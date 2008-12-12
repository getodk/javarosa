package org.javarosa.xform.util.test;

import j2meunit.framework.Test;
import j2meunit.framework.TestCase;
import j2meunit.framework.TestMethod;
import j2meunit.framework.TestSuite;

import java.util.Date;

import org.javarosa.core.model.data.DateData;
import org.javarosa.core.model.data.IntegerData;
import org.javarosa.core.model.data.SelectOneData;
import org.javarosa.core.model.data.StringData;
import org.javarosa.core.model.data.TimeData;
import org.javarosa.core.model.instance.QuestionDataElement;
import org.javarosa.core.model.instance.test.QuestionDataElementTests;
import org.javarosa.xform.util.XFormAnswerDataSerializer;

/**
 * Note that this is just a start and doesn't cover direct comparisons
 * for most values.
 * 
 * @author Clayton Sims
 *
 */
public class XFormAnswerDataSerializerTest extends TestCase{
	final String stringDataValue = "String Data Value";
	final Integer integerDataValue = new Integer(5);
	final Date dateDataValue = new Date();
	final Date timeDataValue = new Date();
	
	StringData stringData;
	IntegerData integerData;
	DateData dateData;
	SelectOneData selectData;
	TimeData timeData;
	
	QuestionDataElement stringElement;
	QuestionDataElement intElement;
	QuestionDataElement dateElement;
	QuestionDataElement selectElement;
	QuestionDataElement timeElement;
	
	XFormAnswerDataSerializer serializer;
	
	private static int NUM_TESTS = 5;
	
	/* (non-Javadoc)
	 * @see j2meunit.framework.TestCase#setUp()
	 */
	protected void setUp() throws Exception {
		stringData = new StringData(stringDataValue);
		stringElement.setValue(stringData);
		
		integerData = new IntegerData(integerDataValue);
		intElement.setValue(integerData);
		
		dateData = new DateData(dateDataValue); 
		dateElement.setValue(dateData);
		
		timeData = new TimeData(timeDataValue); 
		timeElement.setValue(timeData);
		
		serializer = new XFormAnswerDataSerializer();
		
	
	}
	public XFormAnswerDataSerializerTest(String name, TestMethod rTestMethod) {
		super(name, rTestMethod);
	}

	public XFormAnswerDataSerializerTest(String name) {
		super(name);
	}

	public XFormAnswerDataSerializerTest() {
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
			case 1: testString(); break;
			case 2: testInteger(); break;
			case 3: testDate(); break;
			case 4: testTime(); break;
			case 5: testSelect(); break;
		}
	}
	
	public void testString() {
		assertTrue("Serializer Incorrectly Reports Inability to Serializer String", serializer.canSerialize(stringElement));
		Object answerData = serializer.serializeAnswerData(stringData);
		assertNotNull("Serializer returns Null for valid String Data", answerData);
		assertEquals("Serializer returns incorrect string serialization", answerData, stringDataValue);
	}
	
	public void testInteger() {
		assertTrue("Serializer Incorrectly Reports Inability to Serializer Integer", serializer.canSerialize(intElement));
		Object answerData = serializer.serializeAnswerData(integerData);
		assertNotNull("Serializer returns Null for valid Integer Data", answerData);
		//assertEquals("Serializer returns incorrect Integer serialization", answerData, integerDataValue);
	}
	
	public void testDate() {
		assertTrue("Serializer Incorrectly Reports Inability to Serializer Date", serializer.canSerialize(dateElement));
		Object answerData = serializer.serializeAnswerData(dateData);
		assertNotNull("Serializer returns Null for valid Date Data", answerData);
	}
	
	public void testTime() {
		assertTrue("Serializer Incorrectly Reports Inability to Serializer Time", serializer.canSerialize(timeElement));
		Object answerData = serializer.serializeAnswerData(timeData);
		assertNotNull("Serializer returns Null for valid Time Data", answerData);
	}
	
	public void testSelect() {
		//No select tests yet.
	}
}
