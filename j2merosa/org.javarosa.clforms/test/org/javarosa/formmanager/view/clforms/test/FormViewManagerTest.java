package org.javarosa.formmanager.view.clforms.test;

import j2meunit.framework.Test;
import j2meunit.framework.TestCase;
import j2meunit.framework.TestMethod;
import j2meunit.framework.TestSuite;

import org.javarosa.core.model.FormDef;
import org.javarosa.formmanager.controller.FormEntryController;
import org.javarosa.formmanager.model.FormEntryModel;
import org.javarosa.formmanager.view.clforms.*;


public class FormViewManagerTest extends TestCase{
	
	public FormViewManagerTest(String name, TestMethod rTestMethod) {
		super(name, rTestMethod);
	}
	
	public FormViewManagerTest(String name) {
		super(name);
	}
	
	public FormViewManagerTest()
	{
		super();
	}
	
	public Test suite() {
		TestSuite aSuite = new TestSuite();
		
		for (int i = 1; i <= NUM_TESTS; i++) {
			final int testID = i;
			aSuite.addTest(new FormViewManagerTest("FormViewManager Test " + i, new TestMethod() {
				public void run (TestCase tc) {
					((FormViewManagerTest)tc).doTest(testID);
				}
			}));
		}
		return aSuite;
	}
	
	public final static int NUM_TESTS = 3;
	
	public void doTest (int i) 
	{
		switch(i)
		{
			case 1: testgetView();break;
			case 2: testshowFormView();break;
			case 3: testgetIndex();break;
		}
	}


	public void testgetView()
	{

	}
	
	public void testshowFormView()
	{
		FormDef fd = new FormDef();
		FormEntryModel model = new FormEntryModel(fd);
		FormEntryController controller = new FormEntryController(model,null);//shouldn't have null parent
		FormViewManager fvm = new FormViewManager("Test123",model,controller);

		fvm.setShowOverView(true);
		if(fvm.isShowOverView() != true)
			fail("FormViewManager sets showFormView incorrectly");
	}
	
	public void testgetIndex()
	{
		FormDef fd = new FormDef();
		FormEntryModel model = new FormEntryModel(fd);
		FormEntryController controller = new FormEntryController(model,null);//shouldn't have null parent
		FormViewManager fvm = new FormViewManager("Test123",model,controller);


	if(fvm.getIndex() != -1)
		fail("FormViewManager returns incorrect index");

	}
}