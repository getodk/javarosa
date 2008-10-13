package org.javarosa.core.model.storage.test;

import j2meunit.framework.Test;
import j2meunit.framework.TestCase;
import j2meunit.framework.TestMethod;
import j2meunit.framework.TestSuite;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;


import org.javarosa.core.model.instance.DataModelTree;
import org.javarosa.core.services.storage.utilities.MetaDataObject;
import org.javarosa.core.util.UnavailableExternalizerException;
import org.javarosa.core.model.storage.DataModelTreeMetaData;
import org.javarosa.core.model.instance.DataModelTree;
import org.javarosa.core.model.instance.test.QuestionDataGroupTests;



public class DataModelTreeMetaDataTests extends TestCase{

	 
	
	
	private static int NUM_TESTS = 3;
	public void setUp() throws Exception{
		super.setUp();
		
	}
	
	public DataModelTreeMetaDataTests(){
		
	}
	
	public DataModelTreeMetaDataTests(String name){
		super(name);
	}
	
	public DataModelTreeMetaDataTests(String name,TestMethod rTestMethod){
		super(name,rTestMethod);
	}
	
	
	public Test suite(){
		TestSuite aSuite = new TestSuite();
		
		for(int i=1;i<=NUM_TESTS;i++)
		{
			final int testID=i;
			aSuite.addTest(new DataModelTreeMetaDataTests("Data Model Tree Data Test "+i,new TestMethod()
			{
				public void run(TestCase tc)
				{
					((DataModelTreeMetaDataTests)tc).testMaster(testID);
				}
			}                                        )
				           );
		}
		return aSuite;
	}
	public void testMaster(int testID)
	{
		switch(testID){
		case 1: testConstructors(); break;
		case 2: testAccessMoifiers();break;
		case 3: testGetDateSaved(); break;
		
		}
	}
	public void testConstructors(){
		DataModelTreeMetaData d ;//= new DataModelTreeMetaData();
		d = new DataModelTreeMetaData();
		if(d.toString()!=null ||d.getName()!=null ||d.getFormIdReference()!=-1){
			fail("Default constructor for DataModelTreeMetaData not properly initialized");
			
		}
		
		DataModelTree data=new DataModelTree();
		d = new DataModelTreeMetaData(data);
		d.setName("Test Name");
		if(!"Test String".equals(d.toString())||!"Test Name".equals(d.getName()) ||d.getFormIdReference()!=data.getFormReferenceId() ){
			fail("The constructor \"DataModelTreeMetaData(data)\" for DataModelTreeMetaData not properly initialized");
		}
	}
	
	public void testAccessMoifiers(){
		DataModelTreeMetaData d = new DataModelTreeMetaData();
		
		d.setName("DataModelTreeMetaData Name");
		if(!"DataModelTreeMetaData Name".equals(d.getName())){
			fail("setter/getter for name is broken");
		}
		
		
	}
	public void testGetDateSaved(){
		
		//DateFormat formatter= new SimpleDateFormat("");
		 DateFormat formatter = new SimpleDateFormat("dd-MMM-yy");
		 Date expectedDate=new Date();
		 try {
			 expectedDate=(Date)formatter.parse("12-Sun-08");
			
		} catch (ParseException e) {
			
			e.printStackTrace();
		}
		
		//Setting the date
		DataModelTreeMetaData d = new DataModelTreeMetaData();
		DataModelTree dmt=new DataModelTree();
		dmt.setDateSaved(expectedDate);
		assertEquals("The date the data represented by this object was taken is erroneos",expectedDate,d.getDateSaved());
	}
}
