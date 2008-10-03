package org.javarosa.user.model.test;


import j2meunit.framework.Test;
import j2meunit.framework.TestCase;
import j2meunit.framework.TestMethod;
import j2meunit.framework.TestSuite;

import org.javarosa.core.util.test.ExternalizableTest;
import org.javarosa.user.model.User;

public class UserTests extends TestCase  {
	public final int NUM_TESTS = 4 ;

	public UserTests(String name, TestMethod rTestMethod) {
		super(name, rTestMethod);
	}

	public UserTests(String name) {
		super(name);
	}

	public UserTests() {
		super();
	}

	public Test suite() {
		TestSuite aSuite = new TestSuite();

		for (int i = 1; i <= NUM_TESTS; i++) {
			final int testID = i;

			aSuite.addTest(new UserTests("User Test " + i, new TestMethod() {
				public void run (TestCase tc) {
					((UserTests)tc).testMaster(testID);
				}
			}));
		}

		return aSuite;
	}

	public void testMaster (int testID) {
		//System.out.println("running " + testID);

		switch (testID) {
		case 1: testAccessorsModifiers(); break;
		case 2: testIsAdminUser(); break;
		case 3: testEquals(); break;
		case 4: testExternalizable(); break;
		}
	}

	public void testAccessorsModifiers () {
		User u = new User();
		u.setUserType("ADMIN");
		if (!u.getUserType().equals("ADMIN")) {
			fail("UserType getter/setter broken");
		}

		u.setUsername("TEST");
		if (!u.getUsername().equals("TEST")) {
			fail("Username getter/setter broken");
		}

		u.setPassword("TEST");
		if (!u.getPassword().equals("TEST")) {
			fail("Password getter/setter broken");
		}

		u.setRecordId(1);
		if (u.getRecordId() != 1) {
			fail("RecordId getter/setter broken");
		}
	}

	public void testIsAdminUser () {
		User u = new User();
		u.setUserType(User.ADMINUSER);
		assertTrue(u.isAdminUser());
	}

	public void testEquals() {
		User u = new User();
		User u2 = new User();
		assertTrue("User.equals Fails on obj with no values set",u.equals(u2));
		u.setUsername("TEST");
		u2.setUsername("TEST");
		assertTrue("User.equals Fails on obj with usernames set",u.equals(u2));
		u.setPassword("TEST");
		u2.setPassword("TEST");
		assertTrue("User.equals Fails on obj with passwords set",u.equals(u2));
		u.setUserType("TEST");
		u2.setUserType("TEST");
		assertTrue("User.equals Fails on obj with userType set",u.equals(u2));
	}

	public void testExternalizable() {
		User u = new User();
		u.setUsername("TEST");
		u.setPassword("TEST");
//		ExternalizableTest.testSerialization(u, this , "Externalising User failed with no member values set");
//		ExternalizableTest.testSerialization(u, this , "Externalising User failed with only username value set");
//		ExternalizableTest.testSerialization(u, this , "Externalising User failed with member values all set");
	}
}
