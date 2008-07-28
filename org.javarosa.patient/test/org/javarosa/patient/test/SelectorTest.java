package org.javarosa.patient.test;

import java.util.Vector;

import org.javarosa.patient.util.SelectorParser;

import j2meunit.framework.Test;
import j2meunit.framework.TestCase;
import j2meunit.framework.TestMethod;
import j2meunit.framework.TestSuite;

public class SelectorTest extends TestCase  {
		
		public SelectorTest(String name, TestMethod rTestMethod) {
			super(name, rTestMethod);
		}
		
		public SelectorTest(String name) {
			super(name);
		}
		
		public SelectorTest() {
			super();
		}	
		
		public Test suite() {
			TestSuite aSuite = new TestSuite();
			aSuite.addTest((new SelectorTest("Test Basic Parsing", new TestMethod() {
				public void run(TestCase tc) { ((SelectorTest)tc).testBasicParsing(); }
			})));
			aSuite.addTest((new SelectorTest("Test Selection", new TestMethod() {
				public void run(TestCase tc) { ((SelectorTest)tc).testSelection(); }
			})));
			aSuite.addTest((new SelectorTest("Test Selection", new TestMethod() {
				public void run(TestCase tc) { ((SelectorTest)tc).testBestEfforts(); }
			})));
					
			return aSuite;
		}
		
		public void testBasicParsing() {
			Vector testVector = new Vector();
			testVector.addElement("1");
			testVector.addElement("2");
			testVector.addElement("3");
			testVector.addElement("4");
			testVector.addElement("5");
			testVector.addElement("6");
			testVector.addElement("7");
			
			//First just test if we _crash_
			SelectorParser.selectValues("[0:6]",testVector);
			SelectorParser.selectValues("[:7:6]",testVector);
			SelectorParser.selectValues("[0:7:]",testVector);
			SelectorParser.selectValues("[0:N]",testVector);
			SelectorParser.selectValues("[:7:N]",testVector);
			
			//Now test best effort selection
			SelectorParser.selectValues("[0:3]",testVector);
			SelectorParser.selectValues("[:10:6]",testVector);
			SelectorParser.selectValues("[0:10:]",testVector);
			SelectorParser.selectValues("[-4:N]",testVector);
			SelectorParser.selectValues("[:10:N]",testVector);
			SelectorParser.selectValues("[0:12:]",testVector);
		}
		public void testSelection() {
			Vector testVector = new Vector();
			testVector.addElement("1");
			testVector.addElement("2");
			testVector.addElement("3");
			testVector.addElement("4");
			testVector.addElement("5");
			testVector.addElement("6");
			testVector.addElement("7");
			
			Vector headVector = new Vector();
			headVector.addElement("1");
			headVector.addElement("2");
			headVector.addElement("3");
			
			Vector subvector = new Vector();
			subvector.addElement("4");
			subvector.addElement("5");
			subvector.addElement("6");
			
			Vector tailVector = new Vector();
			tailVector.addElement("5");
			tailVector.addElement("6");
			tailVector.addElement("7");
			
			assertEquals("Identity Vector not returned with manual bounds", testVector, SelectorParser.selectValues("[0:6]",testVector));
			assertEquals("Identity Vector not returned with manual bound and index", testVector, SelectorParser.selectValues("[:7:6]",testVector));
			assertEquals("Identity Vector not returned with manual bound and index", testVector, SelectorParser.selectValues("[0:7:]",testVector));
			assertEquals("Identity Vector not returned with dynamic bounds", testVector, SelectorParser.selectValues("[0:N]",testVector));
			assertEquals("Identity Vector not returned with dynamic bound and index", testVector, SelectorParser.selectValues("[:7:N]",testVector));
			
			assertEquals("Head Vector not returned with manuals bounds", headVector,SelectorParser.selectValues("[0:2]",testVector));
			assertEquals("Head Vector not returned with manuals bound and index", headVector,SelectorParser.selectValues("[0:3:]",testVector));
			
			assertEquals("Tail Vector not returned with manuals bounds", tailVector,SelectorParser.selectValues("[4:6]",testVector));
			assertEquals("Tail Vector not returned with manuals bound and index", tailVector,SelectorParser.selectValues("[4:3:]",testVector));
			assertEquals("Tail Vector not returned with manuals bound and index", tailVector,SelectorParser.selectValues("[:3:6]",testVector));
			assertEquals("Tail Vector not returned with dynamic bound and index", tailVector,SelectorParser.selectValues("[:3:N]",testVector));
		}
		
		public void testBestEfforts() {
			Vector testVector = new Vector();
			testVector.addElement("1");
			testVector.addElement("2");
			testVector.addElement("3");
			testVector.addElement("4");
			testVector.addElement("5");
			testVector.addElement("6");
			testVector.addElement("7");
			
			Vector headVector = new Vector();
			headVector.addElement("1");
			headVector.addElement("2");
			headVector.addElement("3");
			
			Vector subvector = new Vector();
			subvector.addElement("4");
			subvector.addElement("5");
			subvector.addElement("6");
			
			Vector tailVector = new Vector();
			tailVector.addElement("5");
			tailVector.addElement("6");
			tailVector.addElement("7");
			
			//Now test best effort selection
			assertEquals("Head Vector not returned with Best Effort and Manual Bounds", headVector, SelectorParser.selectValues("[-2:2]",testVector));
			assertEquals("Identity Vector not returned with Best Effort and Manual Bound and Index", testVector, SelectorParser.selectValues("[:10:6]",testVector));
			assertEquals("Identity Vector not returned with Best Effort and Manual Bound and Index", testVector, SelectorParser.selectValues("[0:10:]",testVector));
			assertEquals("Identity Vector not returned with Best Effort and Dynamic Bounds", testVector, SelectorParser.selectValues("[-4:N]",testVector));
			assertEquals("Identity Vector not returned with Best Effort and Dynamic Bound and Index", testVector, SelectorParser.selectValues("[:10:N]",testVector));
			assertEquals("Tail Vector not returned with Best Effort and Manual Bound and Index", tailVector, SelectorParser.selectValues("[4:12:]",testVector));
		}
}
