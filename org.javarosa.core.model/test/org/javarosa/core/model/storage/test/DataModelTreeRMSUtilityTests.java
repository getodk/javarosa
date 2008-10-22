package org.javarosa.core.model.storage.test;

import j2meunit.framework.Test;
import j2meunit.framework.TestCase;
import j2meunit.framework.TestMethod;
import j2meunit.framework.TestSuite;

import org.javarosa.core.model.storage.DataModelTreeMetaData;
import org.javarosa.core.model.storage.DataModelTreeRMSUtility;


public class DataModelTreeRMSUtilityTests extends TestCase{
	
	
	/**
	 * This is the number of tests
	 */
	private static int NUM_TESTS = 3;
public DataModelTreeRMSUtilityTests(){
	super();
}

public DataModelTreeRMSUtilityTests(String name){
	super(name);
}
public DataModelTreeRMSUtilityTests(String name,TestMethod rTestMethod){
	super(name,rTestMethod);
}

public void setUp() throws Exception{
	super.setUp();
	
}

public void testMaster(int testID)
{
	switch(testID){
	/**
	case 1: testConstructors(); break;
	case 2: testAccessMoifiers();break;
	case 3: testGetDateSaved(); break;
	*/
	
	}
}

public Test suite(){
	TestSuite aSuite = new TestSuite();
	
	for(int i=1;i<=NUM_TESTS;i++)
	{
		final int testID=i;
		aSuite.addTest(new DataModelTreeRMSUtilityTests("Data Model Tree RMS Utility Test "+i,new TestMethod()
		{
			public void run(TestCase tc)
			{
				((DataModelTreeRMSUtilityTests)tc).testMaster(testID);
			}
		}                                        )
			           );
	}
	return aSuite;
}

public void testConstructors(){
	DataModelTreeRMSUtility d;
	d=new DataModelTreeRMSUtility("Utility_name");
	
	if(DataModelTreeRMSUtility.getUtilityName()!=null || d.getListOfFormNames()!=null || d.getFormMetaDataList()!= null){
		fail("The default construtor \"DataModelTreeRMSUtility(Utility_name)\" did not initialize properly");
	}
	
}

public void testGetSize(){
	DataModelTreeRMSUtility d =new DataModelTreeRMSUtility("Utility_name");
	
	DataModelTreeMetaData expectedXformMetaData =d.getMetaDataFromId(1);
	assertEquals("The size of record is not proper!",expectedXformMetaData.getSize(),d.getSize(1));
	//if a method were declared as private, would it be possible to change it to being public just for the case of testing purposes?
	
}

public void testGetName(){

	DataModelTreeRMSUtility d =new DataModelTreeRMSUtility("Utility_name");
	
	DataModelTreeMetaData expectedXformMetaData =d.getMetaDataFromId(1);
	assertEquals("The name of record 1 is not proper!",expectedXformMetaData.getName(),d.getName(1));
}


}
