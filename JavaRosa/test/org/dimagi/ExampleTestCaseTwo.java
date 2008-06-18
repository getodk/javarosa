package org.dimagi;

import junit.framework.TestCase;

public class ExampleTestCaseTwo extends TestCase {
    
    public void testSub() {
        assertTrue("Subtraction is broken!",2==3-1);
    }
    public void testDiv() {
        assertTrue("Division is broken!",4/2==2);
    }

}
