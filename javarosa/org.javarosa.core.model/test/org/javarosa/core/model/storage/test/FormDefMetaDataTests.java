/*
 * Copyright (C) 2009 JavaRosa-Core Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package org.javarosa.core.model.storage.test;

import j2meunit.framework.Test;
import j2meunit.framework.TestCase;
import j2meunit.framework.TestMethod;
import j2meunit.framework.TestSuite;

import org.javarosa.core.model.FormDef;
import org.javarosa.core.model.storage.FormDefMetaData;


public class FormDefMetaDataTests extends TestCase{
private static int NUM_TESTS=3;
	
	public void setUp() throws Exception{
		super.setUp();
	}
	
	public FormDefMetaDataTests(){
		super();
	}
	public FormDefMetaDataTests(String name){
		super(name);
	}
	
	public FormDefMetaDataTests(String name, TestMethod rTestMethod){
		super(name,rTestMethod);
	}
	public Test suite(){
		TestSuite aSuite = new TestSuite();
		
		for(int i=1;i<=NUM_TESTS;i++)
		{
			final int testID=i;
			aSuite.addTest(new FormDefMetaDataTests("FormDef Meta Data Test "+i,new TestMethod()
			{
				public void run(TestCase tc)
				{
					((FormDefMetaDataTests)tc).testMaster(testID);
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
		case 3: testReadExternal(); break;
		
		}
	}
	
	public void testConstructors(){
		FormDefMetaData formDef;//=new FormDefMetaData();
		formDef=new FormDefMetaData();
		
		if(formDef.toString() == null || formDef.getName() == null){
			
			
			fail("The default constructor for \"FormDefMetaData()\" was not properly initialized");
		}
	
		FormDef form=new FormDef();
		formDef=new FormDefMetaData(form);
		if(formDef.toString()==null ){//formDef.getName()== null
			
			
			fail("The constructor for \"FormDefMetaData(FormDef form)\" was not properly initialized");
			
		}
	}
	
	public void testAccessMoifiers(){
		FormDefMetaData formDef=new FormDefMetaData();
		formDef.setName("Name_of_Form");
		if(!formDef.getName().equals("Name_of_Form"))
		{
			fail("Name getter/setter is broken");
		}
		
	}
	
	public void testReadExternal(){
		
		//test type and version
		
	}
}
