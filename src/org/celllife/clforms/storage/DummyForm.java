package org.celllife.clforms.storage;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import org.celllife.clforms.api.Form;
import org.celllife.clforms.xml.XMLUtil;


public class DummyForm implements Externalizable, IDRecordable {
	
	private static byte[] testXForm = new String(""+
			"<html xmlns=\"http://www.w3.org/1999/xhtml\" xmlns:xf=\"http://www.w3.org/2002/xforms\" xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\">\r\n" + 
			"   <head>\r\n" + 
			"      <title>Hello World in XForms</title>\r\n" + 
			"      <xf:model id=\"myform\">\r\n" + 
			"         <xf:instance xmlns=\"\">\r\n" + 
			"            <data>\r\n" + 
			"               <PersonGivenName>\r\n" + 
			"                   <FirstName></FirstName>\r\n" + 
			"                   <SecondName></SecondName>\r\n" + 
			"               </PersonGivenName>\r\n" + 
			"               <DateOfBirth></DateOfBirth>\r\n" + 
			"               <Gender></Gender>\r\n" + 
			"               <CurrentART></CurrentART>\r\n" + 
			"               <isTall></isTall>\r\n" + 
			"            </data>\r\n" + 
			"         </xf:instance>\r\n" + 
			"         <xf:bind id=\"firstName\" nodeset=\"/data/PersonGivenName/FirstName\" type=\"xsd:string\"/>\r\n" + 
			"         <xf:bind id=\"secondName\" nodeset=\"/data/PersonGivenName/SecondName\" type=\"xsd:string\"/>\r\n" + 
			"         <xf:bind id=\"dob\" nodeset=\"/data/DateOfBirth\" type=\"xsd:date\"/>\r\n" + 
			"         <xf:bind id=\"gender\" nodeset=\"/data/Gender\" type=\"xsd:string\"/>\r\n" + 
			"         <xf:bind id=\"CurrentART\" nodeset=\"/data/CurrentART\" type=\"xsd:string\"/>\r\n" + 
			"         <xf:bind id=\"tall\" nodeset=\"/data/isTall\" type=\"xsd:boolean\"/>\r\n" + 
			"      </xf:model>\r\n" + 
			"   </head>\r\n" + 
			"   <body>\r\n" + 
			"      <p>Type in your first name in the input box. <br/>\r\n" + 
			"        If you are running XForms, the output should be displayed in the output area.</p>   \r\n" + 
			"         <xf:input bind=\"firstName\" incremental=\"true\">\r\n" + 
			"            <xf:label>Please enter your first name: </xf:label>\r\n" + 
			"            <xf:hint>First name...</xf:hint>            \r\n" + 
			"         </xf:input>\r\n" + 
			"         <xf:input bind=\"secondName\" incremental=\"true\">\r\n" + 
			"            <xf:label>Please enter your second name: </xf:label>\r\n" + 
			"            <xf:hint>Second name...</xf:hint>            \r\n" + 
			"         </xf:input>\r\n" + 
			"          <xf:input bind=\"dob\" incremental=\"true\">\r\n" + 
			"            <xf:label>Please enter DOB:</xf:label>\r\n" + 
			"         </xf:input>\r\n" + 
			"          <xf:input bind=\"tall\" incremental=\"true\">\r\n" + 
			"            <xf:label>Are you tall?</xf:label>\r\n" + 
			"            <xf:hint>relatively....</xf:hint>            \r\n" + 
			"         </xf:input>\r\n" + 
			"         <xf:select1 bind=\"gender\" selection=\"closed\" appearance=\"full\" >  \r\n" + 
			"            <xf:label>Please choose a gender</xf:label> \r\n" + 
			"            <xf:hint>Select 1...</xf:hint>            \r\n" + 
			"	    	<xf:item>\r\n" + 
			"                <xf:label>Female</xf:label>\r\n" + 
			"                <xf:value>Female</xf:value> \r\n" + 
			"            </xf:item>\r\n" + 
			"            <xf:item>\r\n" + 
			"                <xf:label>Male</xf:label>\r\n" + 
			"                <xf:value>Male</xf:value>\r\n" + 
			"            </xf:item>\r\n" + 
			"            <xf:item>\r\n" + 
			"                <xf:label>Other</xf:label>\r\n" + 
			"                <xf:value>Other</xf:value>\r\n" + 
			"            </xf:item>\r\n" + 
			"       </xf:select1>         <br />\r\n" + 
			"<xf:select bind=\"CurrentART\" selection=\"closed\" appearance=\"full\" > \r\n" + 
			"	<xf:label>CurrentART:</xf:label> \r\n" + 
			"	<xf:hint>more than one option can be selected...</xf:hint>\r\n" + 
			"            <xf:item>\r\n" + 
			"                <xf:label>3TC</xf:label>\r\n" + 
			"                <xf:value>3TC</xf:value> \r\n" + 
			"            </xf:item>\r\n" + 
			"            <xf:item>\r\n" + 
			"                <xf:label>AZT</xf:label>\r\n" + 
			"                <xf:value>AZT</xf:value>\r\n" + 
			"            </xf:item>\r\n" + 
			"            <xf:item>\r\n" + 
			"                <xf:label>EFV</xf:label>\r\n" + 
			"                <xf:value>EFV</xf:value>\r\n" + 
			"            </xf:item>\r\n" + 
			"            <xf:item>\r\n" + 
			"                <xf:label>NVP</xf:label>\r\n" + 
			"                <xf:value>NVP</xf:value>\r\n" + 
			"            </xf:item>\r\n" + 
			"       </xf:select>         \r\n" + 
			"   </body>\r\n" + 
			"</html>"
			+"").getBytes(); //
	
	private static byte[] testDefaultDataXForm = new String(""+
			"<html xmlns=\"http://www.w3.org/1999/xhtml\" xmlns:xf=\"http://www.w3.org/2002/xforms\" xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\">\r\n" + 
			"   <head>\r\n" + 
			"      <title>Hello World in XForms</title>\r\n" + 
			"      <xf:model id=\"Demo Form\">\r\n" + 
			"         <xf:instance xmlns=\"\">\r\n" + 
			"<data>" + 
			"<PersonGivenName>" + 
			"<FirstName>Default.firstname</FirstName>" + 
			"<SecondName>Default.secondname</SecondName>" + 
			"</PersonGivenName>" + 
			"<DateOfBirth>07/10/1982</DateOfBirth>" + 
			"<Gender>Male</Gender>" + 
			"<CurrentART>[AZT,EFV]</CurrentART>" + 
			"<isTall>false</isTall>" + 
			"</data>" + 
			"         </xf:instance>\r\n" + 
			"         <xf:bind id=\"firstName\" nodeset=\"/data/PersonGivenName/FirstName\" type=\"xsd:string\"/>\r\n" + 
			"         <xf:bind id=\"secondName\" nodeset=\"/data/PersonGivenName/SecondName\" type=\"xsd:string\"/>\r\n" + 
			"         <xf:bind id=\"dob\" nodeset=\"/data/DateOfBirth\" type=\"xsd:date\"/>\r\n" + 
			"         <xf:bind id=\"gender\" nodeset=\"/data/Gender\" type=\"xsd:string\"/>\r\n" + 
			"         <xf:bind id=\"CurrentART\" nodeset=\"/data/CurrentART\" type=\"xsd:string\"/>\r\n" + 
			"         <xf:bind id=\"tall\" nodeset=\"/data/isTall\" type=\"xsd:boolean\"/>\r\n" + 
			"      </xf:model>\r\n" + 
			"   </head>\r\n" + 
			"   <body>\r\n" + 
			"      <p>Type in your first name in the input box. <br/>\r\n" + 
			"        If you are running XForms, the output should be displayed in the output area.</p>   \r\n" + 
			"         <xf:input bind=\"firstName\" incremental=\"true\">\r\n" + 
			"            <xf:label>Please enter your first name: </xf:label>\r\n" + 
			"            <xf:hint>First name...</xf:hint>            \r\n" + 
			"         </xf:input>\r\n" + 
			"         <xf:input bind=\"secondName\" incremental=\"true\">\r\n" + 
			"            <xf:label>Please enter your second name: </xf:label>\r\n" + 
			"            <xf:hint>Second name...</xf:hint>            \r\n" + 
			"         </xf:input>\r\n" + 
			"          <xf:input bind=\"dob\" incremental=\"true\">\r\n" + 
			"            <xf:label>Please enter DOB:</xf:label>\r\n" + 
			"         </xf:input>\r\n" + 
			"         <xf:select1 bind=\"gender\" selection=\"closed\" appearance=\"full\" >  \r\n" + 
			"            <xf:label>Please choose a gender</xf:label> \r\n" + 
			"            <xf:hint>Select 1...</xf:hint>            \r\n" + 
			"	    	<xf:item>\r\n" + 
			"                <xf:label>Female</xf:label>\r\n" + 
			"                <xf:value>Female</xf:value> \r\n" + 
			"            </xf:item>\r\n" + 
			"            <xf:item>\r\n" + 
			"                <xf:label>Male</xf:label>\r\n" + 
			"                <xf:value>Male</xf:value>\r\n" + 
			"            </xf:item>\r\n" + 
			"            <xf:item>\r\n" + 
			"                <xf:label>Other</xf:label>\r\n" + 
			"                <xf:value>Other</xf:value>\r\n" + 
			"            </xf:item>\r\n" + 
			"       </xf:select1>         <br />\r\n" + 
			"<xf:select bind=\"CurrentART\" selection=\"closed\" appearance=\"full\" > \r\n" + 
			"	<xf:label>CurrentART:</xf:label> \r\n" + 
			"	<xf:hint>more than one option can be selected...</xf:hint>\r\n" + 
			"            <xf:item>\r\n" + 
			"                <xf:label>3TC</xf:label>\r\n" + 
			"                <xf:value>3TC</xf:value> \r\n" + 
			"            </xf:item>\r\n" + 
			"            <xf:item>\r\n" + 
			"                <xf:label>AZT</xf:label>\r\n" + 
			"                <xf:value>AZT</xf:value>\r\n" + 
			"            </xf:item>\r\n" + 
			"            <xf:item>\r\n" + 
			"                <xf:label>EFV</xf:label>\r\n" + 
			"                <xf:value>EFV</xf:value>\r\n" + 
			"            </xf:item>\r\n" + 
			"            <xf:item>\r\n" + 
			"                <xf:label>NVP</xf:label>\r\n" + 
			"                <xf:value>NVP</xf:value>\r\n" + 
			"            </xf:item>\r\n" + 
			"       </xf:select>         \r\n" + 
			"   </body>\r\n" + 
			"</html>"
			+"").getBytes(); //
	
	
	private static byte[] relevantTestXForm = new String(""+
			"<html xmlns=\"http://www.w3.org/1999/xhtml\" xmlns:xf=\"http://www.w3.org/2002/xforms\" xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\">\r\n" + 
			"   <head>\r\n" + 
			"      <title>Munier was here...</title>\r\n" + 
			"      <xf:model id=\"relevancy Form\">\r\n" + 
			"         <xf:instance xmlns=\"\">\r\n" + 
			"<data>" + 
			"<givenName>TEMP</givenName>" + 
			"<gender>Other</gender>" + 
			"<isPregnant></isPregnant>" + 
			"</data>" + 
			"</xf:instance>"+ 
			"         <xf:bind id=\"givenName\" nodeset=\"/data/givenName\" type=\"xsd:string\"/>\r\n" + 
			"         <xf:bind id=\"gender\" nodeset=\"/data/gender\" type=\"xsd:string\"/>\r\n" + 
			"         <xf:bind id=\"isPregnant\" nodeset=\"/data/isPregnant\" type=\"xsd:boolean\"/>\r\n" + 
			"      </xf:model>\r\n" + 
			"   </head>\r\n" + 
			"   <body>\r\n" + 
			"         <xf:input bind=\"givenName\" >\r\n" + 
			"            <xf:label>Please enter your name: </xf:label>\r\n" + 
			"            <xf:hint>...name...</xf:hint>            \r\n" + 
			"         </xf:input>\r\n" + 
			"         <xf:select1 bind=\"gender\" >  \r\n" + 
			"            <xf:label>Please choose a gender: </xf:label> \r\n" + 
			"            <xf:hint>Select 1...</xf:hint>            \r\n" + 
			"	    	   <xf:item>\r\n" + 
			"                <xf:label>Female</xf:label>\r\n" + 
			"                <xf:value>Female</xf:value> \r\n" + 
			"            </xf:item>\r\n" + 
			"            <xf:item>\r\n" + 
			"                <xf:label>Male</xf:label>\r\n" + 
			"                <xf:value>Male</xf:value>\r\n" + 
			"            </xf:item>\r\n" + 
			"            <xf:item>\r\n" + 
			"                <xf:label>Other</xf:label>\r\n" + 
			"                <xf:value>Other</xf:value>\r\n" + 
			"            </xf:item>\r\n" + 
			"          </xf:select1>         <br />\r\n" + 
			"          <xf:input bind=\"isPregnant\" relevant=\"/data/gender='Female'\">\r\n" + 
			"            <xf:label>Are you pregnant? </xf:label>\r\n" + 
			"            <xf:hint>... today ....</xf:hint>            \r\n" + 
			"         </xf:input>\r\n" +
			"          <xf:input bind=\"isPregnant\" relevant=\"/data/gender='Male'\">\r\n" + 
			"            <xf:label>Are you a Father? </xf:label>\r\n" + 
			"            <xf:hint>... today ....</xf:hint>            \r\n" + 
			"         </xf:input>\r\n" +
			"   </body>\r\n" + 
			"</html>"
			+"").getBytes(); //
	
	private static byte[] mobileSurvey = new String("" +
			"<xf:html xmlns:xf=\"http://www.w3.org/2002/xforms\">\r\n" + 
			"  <head>\r\n" + 
			"    <title>Test title</title>\r\n" + 
			"    <xf:model id=\"MobileSurvey\">\r\n" + 
			"      <xf:instance>\r\n" + 
			"        <data>\r\n" + 
			"          <Subject>\r\n" + 
			"            <Age />\r\n" + 
			"            <Gender />\r\n" + 
			"            <Homearea />\r\n" + 
			"          </Subject>\r\n" + 
			"          <Mobile>\r\n" + 
			"            <Brand />\r\n" + 
			"            <Model />\r\n" + 
			"            <Payment />\r\n" + 
			"	    <Services />\r\n" + 
			"	  </Mobile>\r\n" + 
			"          <DataCapture>\r\n" + 
			"	    <EnteredBy />\r\n" + 
			"          </DataCapture>\r\n" + 
			"        </data>\r\n" + 
			"      </xf:instance>\r\n" + 
			"      <xf:bind id=\"Age\" nodeset=\"/data/Subject/Age\" type=\"xsd:string\" />\r\n" + 
			"      <xf:bind id=\"Gender\" nodeset=\"/data/Subject/Gender\" type=\"xsd:string\" />\r\n" + 
			"      <xf:bind id=\"Homearea\" nodeset=\"/data/Subject/Homearea\" type=\"xsd:string\" />\r\n" + 
			"      <xf:bind id=\"Brand\" nodeset=\"/data/Mobile/Brand\" type=\"xsd:string\" />\r\n" + 
			"      <xf:bind id=\"Model\" nodeset=\"/data/Mobile/Model\" type=\"xsd:string\" />\r\n" + 
			"      <xf:bind id=\"Payment\" nodeset=\"/data/Mobile/Payment\" type=\"xsd:string\" />\r\n" + 
			"      <xf:bind id=\"Services\" nodeset=\"/data/Mobile/Services\" type=\"xsd:string\" />\r\n" + 
			"      <xf:bind id=\"EnteredBy\" nodeset=\"/data/DataCapture/EnteredBy\" type=\"xsd:string\" />\r\n" + 
			"    </xf:model>\r\n" + 
			"  </head>\r\n" + 
			"  <body>\r\n" + 
			"    <xf:select1 bind=\"Age\">\r\n" + 
			"      <xf:label>How old are you?</xf:label>\r\n" + 
			"      <xf:hint>Select your relevent age group</xf:hint>\r\n" + 
			"      <xf:item>\r\n" + 
			"        <xf:label>Under 15</xf:label>\r\n" + 
			"        <xf:value>0-15</xf:value>\r\n" + 
			"      </xf:item>\r\n" + 
			"      <xf:item>\r\n" + 
			"        <xf:label>15-20</xf:label>\r\n" + 
			"        <xf:value>15-20</xf:value>\r\n" + 
			"      </xf:item>\r\n" + 
			"      <xf:item>\r\n" + 
			"        <xf:label>21-30</xf:label>\r\n" + 
			"        <xf:value>21-30</xf:value>\r\n" + 
			"      </xf:item>\r\n" + 
			"      <xf:item>\r\n" + 
			"        <xf:label>31-45</xf:label>\r\n" + 
			"        <xf:value>31-45</xf:value>\r\n" + 
			"      </xf:item>\r\n" + 
			"      <xf:item>\r\n" + 
			"        <xf:label>46-59</xf:label>\r\n" + 
			"        <xf:value>46-59</xf:value>\r\n" + 
			"      </xf:item>\r\n" + 
			"      <xf:item>\r\n" + 
			"        <xf:label>60 and over</xf:label>\r\n" + 
			"        <xf:value>60+</xf:value>\r\n" + 
			"      </xf:item>\r\n" + 
			"    </xf:select1>\r\n" + 
			"    <xf:select1 bind=\"Gender\">\r\n" + 
			"      <xf:label>Are you male or female?</xf:label>\r\n" + 
			"      <xf:hint>Select 1...</xf:hint>\r\n" + 
			"      <xf:item>\r\n" + 
			"        <xf:label>Female</xf:label>\r\n" + 
			"        <xf:value>Female</xf:value>\r\n" + 
			"      </xf:item>\r\n" + 
			"      <xf:item>\r\n" + 
			"        <xf:label>Male</xf:label>\r\n" + 
			"        <xf:value>Male</xf:value>\r\n" + 
			"      </xf:item>\r\n" + 
			"    </xf:select1>\r\n" + 
			"    <xf:input bind=\"Homearea\">\r\n" + 
			"      <xf:label>Where do you live?</xf:label>\r\n" + 
			"    </xf:input>\r\n" + 
			"    <xf:select1 bind=\"Brand\" selection=\"closed\" appearance=\"full\">\r\n" + 
			"      <xf:label>What brand is your phone?</xf:label>\r\n" + 
			"      <xf:hint>Please select from list...</xf:hint>\r\n" + 
			"      <xf:item>\r\n" + 
			"        <xf:label>Nokia</xf:label>\r\n" + 
			"        <xf:value>Nokia</xf:value>\r\n" + 
			"      </xf:item>\r\n" + 
			"      <xf:item>\r\n" + 
			"        <xf:label>Sony Ericsson</xf:label>\r\n" + 
			"        <xf:value>Sony Ericsson</xf:value>\r\n" + 
			"      </xf:item>\r\n" + 
			"      <xf:item>\r\n" + 
			"        <xf:label>Motorola</xf:label>\r\n" + 
			"        <xf:value>Motorola</xf:value>\r\n" + 
			"      </xf:item>\r\n" + 
			"      <xf:item>\r\n" + 
			"        <xf:label>Samsung</xf:label>\r\n" + 
			"        <xf:value>Samsung</xf:value>\r\n" + 
			"      </xf:item>\r\n" + 
			"      <xf:item>\r\n" + 
			"        <xf:label>LG</xf:label>\r\n" + 
			"        <xf:value>LG</xf:value>\r\n" + 
			"      </xf:item>\r\n" + 
			"    </xf:select1>\r\n" + 
			"    <xf:input bind=\"Model\">\r\n" + 
			"      <xf:label>What model is it?</xf:label>\r\n" + 
			"      <xf:hint>leave blank if unsure...</xf:hint>\r\n" + 
			"    </xf:input>\r\n" + 
			"    <xf:select1 bind=\"Payment\" selection=\"closed\" appearance=\"full\">\r\n" + 
			"      <xf:label>Are you contract or pre-paid?</xf:label>\r\n" + 
			"      <xf:item>\r\n" + 
			"        <xf:label>Contract</xf:label>\r\n" + 
			"        <xf:value>Contract</xf:value>\r\n" + 
			"      </xf:item>\r\n" + 
			"      <xf:item>\r\n" + 
			"        <xf:label>Pre-paid</xf:label>\r\n" + 
			"        <xf:value>Pre-paid</xf:value>\r\n" + 
			"      </xf:item>\r\n" + 
			"    </xf:select1>\r\n" + 
			"    <xf:select bind=\"Services\" selection=\"closed\" appearance=\"full\">\r\n" + 
			"      <xf:label>Which of these have you used on your phone?</xf:label>\r\n" + 
			"      <xf:hint>more than one option can be selected...</xf:hint>\r\n" + 
			"      <xf:item>\r\n" + 
			"        <xf:label>SMS</xf:label>\r\n" + 
			"        <xf:value>SMS</xf:value>\r\n" + 
			"      </xf:item>\r\n" + 
			"      <xf:item>\r\n" + 
			"        <xf:label>MMS</xf:label>\r\n" + 
			"        <xf:value>MMS</xf:value>\r\n" + 
			"      </xf:item>\r\n" + 
			"      <xf:item>\r\n" + 
			"        <xf:label>Email</xf:label>\r\n" + 
			"        <xf:value>Email</xf:value>\r\n" + 
			"      </xf:item>\r\n" + 
			"      <xf:item>\r\n" + 
			"        <xf:label>The Internet</xf:label>\r\n" + 
			"        <xf:value>Internet</xf:value>\r\n" + 
			"      </xf:item>\r\n" + 
			"      <xf:item>\r\n" + 
			"        <xf:label>Mxit</xf:label>\r\n" + 
			"        <xf:value>Mxit</xf:value>\r\n" + 
			"      </xf:item>\r\n" + 
			"      <xf:item>\r\n" + 
			"        <xf:label>Games</xf:label>\r\n" + 
			"        <xf:value>Games</xf:value>\r\n" + 
			"      </xf:item>\r\n" + 
			"      <xf:item>\r\n" + 
			"        <xf:label>Download new games</xf:label>\r\n" + 
			"        <xf:value>Dnewgames</xf:value>\r\n" + 
			"      </xf:item>\r\n" + 
			"    </xf:select>\r\n" + 
			"    <xf:input bind=\"EnteredBy\">\r\n" + 
			"      <xf:label>Please enter name of data capturer:</xf:label>\r\n" + 
			"      <xf:hint>... Cell-Life username ...</xf:hint>\r\n" + 
			"    </xf:input>\r\n" + 
			"  </body>\r\n" + 
			"</xf:html>" +
			"").getBytes(); //
	
	private int recordId = 0;
	private static byte [] currentData = mobileSurvey;//testDefaultDataXForm;//testXForm;//relevantTestXForm;
	public DummyForm()
	{
		recordId = 0;
	}
	
	public void setRecordId(int recordId) {

		this.recordId = recordId;
	}
        
	public Form getXFormObject()
	{
		DataInputStream dis = new DataInputStream(new ByteArrayInputStream(this.currentData));
		InputStreamReader isr = new InputStreamReader(dis);
		return XMLUtil.parseForm(isr);
		
	}

	/**
	 * @param in
	 * @throws IOException
	 */
	public void readExternal(DataInputStream in) throws IOException {

	}

	public void writeExternal(DataOutputStream out) throws IOException {
	    	
	    	String str = new String(currentData);
	    	out.write(currentData);
	}
	
	public static byte[] getData() {
		return currentData;
	}

	public void setDemo() {
		this.currentData = this.mobileSurvey;
		
	}
}


