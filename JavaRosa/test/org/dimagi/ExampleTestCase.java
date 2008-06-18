package org.dimagi;

import junit.framework.TestCase;

public class ExampleTestCase extends TestCase {
    
    public void testAdd() {
        assertTrue("Addition is broken!",2==1+1);
    }
    public void testMul() {
        assertTrue("Multiplication is broken!",2*2==4);
    }

}
