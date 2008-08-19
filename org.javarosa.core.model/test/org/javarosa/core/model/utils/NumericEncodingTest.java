package org.javarosa.core.model.utils;

import j2meunit.framework.Test;
import j2meunit.framework.TestCase;
import j2meunit.framework.TestMethod;
import j2meunit.framework.TestSuite;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import org.javarosa.core.model.utils.ExternalizableHelper;

public class NumericEncodingTest extends TestCase  {
		
		public NumericEncodingTest(String name, TestMethod rTestMethod) {
			super(name, rTestMethod);
		}
		
		public NumericEncodingTest(String name) {
			super(name);
		}
		
		public NumericEncodingTest() {
			super();
		}	
		
		public Test suite() {
			TestSuite aSuite = new TestSuite();
			aSuite.addTest((new NumericEncodingTest("Encoding Test Suite", new TestMethod() {
				public void run(TestCase tc) { ((NumericEncodingTest)tc).numericEncodingUnitTestSuite(); }
			})));
			return aSuite;
		}
		
		public void numericEncodingUnitTest (long valIn, int encoding) {
			byte[] bytesOut; 
			long valOut;
			String testString = "Testing: " + valIn + " [";
			
			switch (encoding) {
			case ExternalizableHelper.ENCODING_NUM_DEFAULT: testString += "default"; break;
			case ExternalizableHelper.ENCODING_NUM_STINGY: testString += "stingy"; break;
			}
			testString += "]";
			
			try {
				ByteArrayOutputStream baos = new ByteArrayOutputStream(100);
				ExternalizableHelper.writeNumeric(new DataOutputStream(baos), valIn, encoding);
				bytesOut = baos.toByteArray();

				ByteArrayInputStream bais = new ByteArrayInputStream(bytesOut);
				valOut = ExternalizableHelper.readNumeric(new DataInputStream(bais), encoding);
			} catch (IOException ioe) {
				fail(testString + "IOException!");
				return;
			}
			
			testString += "; serialized as: [";
			for (int i = 0; i < bytesOut.length; i++) {
				String hex = Integer.toHexString(bytesOut[i]);
				if (hex.length() == 1)
					hex = "0" + hex;
				else
					hex = hex.substring(hex.length() - 2);
				testString += hex;
				if (i < bytesOut.length - 1)
					testString += " ";
			}
			testString += "]; deserialized: ";
			if (valIn != valOut) {
				fail(testString + "Error! [" + valOut + "]");
				return;
			}
		}
		
		public void numericEncodingUnitTestSuite () {
			int enc;
			
			enc = ExternalizableHelper.ENCODING_NUM_DEFAULT;
			
			numericEncodingUnitTest(0, enc);
			numericEncodingUnitTest(-1, enc);
			numericEncodingUnitTest(1, enc);			
			numericEncodingUnitTest(-2, enc);
		
			for (int i = 3; i <= 64; i++) {
				long min = (i < 64 ? -((long)0x01 << (i - 1)) : Long.MIN_VALUE);
				long max = (i < 64 ? ((long)0x01 << (i - 1)) - 1 : Long.MAX_VALUE);
				
				numericEncodingUnitTest(max - 1, enc);
				numericEncodingUnitTest(max, enc);
				if (i < 64)
					numericEncodingUnitTest(max + 1, enc);
				numericEncodingUnitTest(min + 1, enc);
				numericEncodingUnitTest(min, enc);
				if (i < 64)
					numericEncodingUnitTest(min - 1, enc);					
			}
			
			enc = ExternalizableHelper.ENCODING_NUM_STINGY;
			
			for (int i = 0; i <= 1; i++) {
				int offset = (i == 0 ? 0 : ExternalizableHelper.STINGY_NEGATIVE_BIAS);
				if (offset == 0 && i == 1)
					break;
				
				numericEncodingUnitTest(0 - offset, enc);
				numericEncodingUnitTest(1 - offset, enc);
				numericEncodingUnitTest(126 - offset, enc);
				numericEncodingUnitTest(127 - offset, enc);
				numericEncodingUnitTest(128 - offset, enc);
				numericEncodingUnitTest(129 - offset, enc);
				numericEncodingUnitTest(253 - offset, enc);
				numericEncodingUnitTest(254 - offset, enc);
				numericEncodingUnitTest(255 - offset, enc);
				numericEncodingUnitTest(256 - offset, enc);
				numericEncodingUnitTest(-1 - offset, enc);
				numericEncodingUnitTest(-2 - offset, enc);
				numericEncodingUnitTest(-127 - offset, enc);
				numericEncodingUnitTest(-128 - offset, enc);
				numericEncodingUnitTest(-129 - offset, enc);
			}
			numericEncodingUnitTest(3750, enc);
			numericEncodingUnitTest(-3750, enc);
			numericEncodingUnitTest(33947015, enc);
			numericEncodingUnitTest(-33947015, enc);		
			numericEncodingUnitTest(Integer.MAX_VALUE, enc);
			numericEncodingUnitTest(Integer.MAX_VALUE - 1, enc);
			numericEncodingUnitTest(Integer.MIN_VALUE, enc);
			numericEncodingUnitTest(Integer.MIN_VALUE + 1, enc);
		}
}
