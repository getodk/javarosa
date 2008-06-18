package org.javarosa.demo;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.InputStreamReader;

import org.javarosa.clforms.api.Form;
import org.javarosa.clforms.xml.XMLUtil;

public class ExampleForm {

	private static byte[] dimagiSurvey = new String("" +
			"<xf:html xmlns:xf=\"http://www.w3.org/2002/xforms\">\r\n" +
			"  <head>\r\n" +
			"    <title>Title</title>\r\n" +
			"    <xf:model id=\"DimagiSurvey\">\r\n" +
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
			"      <xf:bind id=\"ID\" nodeset=\"/data/Subject/ID\" type=\"xsd:string\" />\r\n" +
			"      <xf:bind id=\"Sex\" nodeset=\"/data/Subject/Sex\" type=\"xsd:string\" />\r\n" +
			"      <xf:bind id=\"Symptoms\" nodeset=\"/data/Subject/Symptoms\" type=\"xsd:string\" />\r\n" +
			"      <xf:bind id=\"City\" nodeset=\"/data/Subject/City\" type=\"xsd:string\" />\r\n" +
			"    </xf:model>\r\n" +
			"  </head>\r\n" +
			"  <body>\r\n" +
			"    <xf:textbox bind=\"ID\">\r\n" +
			"      <xf:label>Enter the patient's ID number.</xf:label>\r\n" +
			"    </xf:textbox>\r\n" +
			"    <xf:select1 bind=\"Sex\">\r\n" +
			"      <xf:label>What is the sex of the patient?</xf:label>\r\n" +
			"      <xf:item>\r\n" +
			"        <xf:label>Female</xf:label>\r\n" +
			"        <xf:value>Female</xf:value>\r\n" +
			"      </xf:item>\r\n" +
			"      <xf:item>\r\n" +
			"        <xf:label>Male</xf:label>\r\n" +
			"        <xf:value>Male</xf:value>\r\n" +
			"      </xf:item>\r\n" +
			"    </xf:select1>\r\n" +
			"    <xf:select bind=\"Symptoms\">\r\n" +
			"      <xf:label>Has the patient had any of the following symptoms since their last visit?</xf:label>\r\n" +
			"      <xf:item>\r\n" +
			"        <xf:label>Fever</xf:label>\r\n" +
			"        <xf:value>Fever</xf:value>\r\n" +
			"      </xf:item>\r\n" +
			"      <xf:item>\r\n" +
			"        <xf:label>Night Sweats</xf:label>\r\n" +
			"        <xf:value>Night Sweats</xf:value>\r\n" +
			"      </xf:item>\r\n" +
			"      <xf:item>\r\n" +
			"        <xf:label>Weight Loss</xf:label>\r\n" +
			"        <xf:value>Weight Loss</xf:value>\r\n" +
			"      </xf:item>\r\n" +
			"      <xf:item>\r\n" +
			"        <xf:label>Vomitting</xf:label>\r\n" +
			"        <xf:value>Vomitting</xf:value>\r\n" +
			"      </xf:item>\r\n" +
			"    </xf:select>\r\n" +
			"    <xf:select1 bind=\"City\">\r\n" +
			"      <xf:label>Where does the patient live?</xf:label>\r\n" +
			"      <xf:item>\r\n" +
			"        <xf:label>Cambridge</xf:label>\r\n" +
			"        <xf:value>Cambridge</xf:value>\r\n" +
			"      </xf:item>\r\n" +
			"      <xf:item>\r\n" +
			"        <xf:label>Boston</xf:label>\r\n" +
			"        <xf:value>Boston</xf:value>\r\n" +
			"      </xf:item>\r\n" +
			"      <xf:item>\r\n" +
			"        <xf:label>Newton</xf:label>\r\n" +
			"        <xf:value>Newton</xf:value>\r\n" +
			"      </xf:item>\r\n" +
			"      <xf:item>\r\n" +
			"        <xf:label>Quincy</xf:label>\r\n" +
			"        <xf:value>Quincy</xf:value>\r\n" +
			"      </xf:item>\r\n" +
			"      <xf:item>\r\n" +
			"        <xf:label>Brookline</xf:label>\r\n" +
			"        <xf:value>Brookline</xf:value>\r\n" +
			"      </xf:item>\r\n" +
			"    </xf:select1>\r\n" +
			"  </body>\r\n" +
			"</xf:html>" +
			"").getBytes(); //

	private static byte[] dimagiSurvey2 = new String("" +
			"<xf:html xmlns:xf=\"http://www.w3.org/2002/xforms\">\r\n" +
			"  <head>\r\n" +
			"    <title>Title</title>\r\n" +
			"    <xf:model id=\"DimagiSurvey2\">\r\n" +
			"      <xf:instance>\r\n" +
			"        <data>\r\n" +
			"          <Subject>\r\n" +
			"            <ID />\r\n" +
			"            <Sex />\r\n" +
			"            <Symptoms />\r\n" +
			"            <City />\r\n" +
			"          </Subject>\r\n" +
			"        </data>\r\n" +
			"      </xf:instance>\r\n" +
			"      <xf:bind id=\"ID\" nodeset=\"/data/Subject/ID\" type=\"xsd:string\" />\r\n" +
			"      <xf:bind id=\"Sex\" nodeset=\"/data/Subject/Sex\" type=\"xsd:string\" />\r\n" +
			"      <xf:bind id=\"Symptoms\" nodeset=\"/data/Subject/Symptoms\" type=\"xsd:string\" />\r\n" +
			"      <xf:bind id=\"City\" nodeset=\"/data/Subject/City\" type=\"xsd:string\" />\r\n" +
			"    </xf:model>\r\n" +
			"  </head>\r\n" +
			"  <body>\r\n" +
			"    <xf:textbox bind=\"ID\">\r\n" +
			"      <xf:label>Enter the patient's ID number.</xf:label>\r\n" +
			"    </xf:textbox>\r\n" +
			"    <xf:select1 bind=\"Sex\">\r\n" +
			"      <xf:label>What is the sex of the patient?</xf:label>\r\n" +
			"      <xf:item>\r\n" +
			"        <xf:label>Female</xf:label>\r\n" +
			"        <xf:value>Female</xf:value>\r\n" +
			"      </xf:item>\r\n" +
			"      <xf:item>\r\n" +
			"        <xf:label>Male</xf:label>\r\n" +
			"        <xf:value>Male</xf:value>\r\n" +
			"      </xf:item>\r\n" +
			"    </xf:select1>\r\n" +
			"    <xf:select bind=\"Symptoms\">\r\n" +
			"      <xf:label>Has the patient had any of the following symptoms since their last visit?</xf:label>\r\n" +
			"      <xf:item>\r\n" +
			"        <xf:label>Fever</xf:label>\r\n" +
			"        <xf:value>Fever</xf:value>\r\n" +
			"      </xf:item>\r\n" +
			"      <xf:item>\r\n" +
			"        <xf:label>Night Sweats</xf:label>\r\n" +
			"        <xf:value>Night Sweats</xf:value>\r\n" +
			"      </xf:item>\r\n" +
			"      <xf:item>\r\n" +
			"        <xf:label>Weight Loss</xf:label>\r\n" +
			"        <xf:value>Weight Loss</xf:value>\r\n" +
			"      </xf:item>\r\n" +
			"      <xf:item>\r\n" +
			"        <xf:label>Vomitting</xf:label>\r\n" +
			"        <xf:value>Vomitting</xf:value>\r\n" +
			"      </xf:item>\r\n" +
			"    </xf:select>\r\n" +
			"    <xf:select1 bind=\"City\">\r\n" +
			"      <xf:label>Where does the patient live?</xf:label>\r\n" +
			"      <xf:item>\r\n" +
			"        <xf:label>Cambridge</xf:label>\r\n" +
			"        <xf:value>Cambridge</xf:value>\r\n" +
			"      </xf:item>\r\n" +
			"      <xf:item>\r\n" +
			"        <xf:label>Boston</xf:label>\r\n" +
			"        <xf:value>Boston</xf:value>\r\n" +
			"      </xf:item>\r\n" +
			"      <xf:item>\r\n" +
			"        <xf:label>Newton</xf:label>\r\n" +
			"        <xf:value>Newton</xf:value>\r\n" +
			"      </xf:item>\r\n" +
			"      <xf:item>\r\n" +
			"        <xf:label>Quincy</xf:label>\r\n" +
			"        <xf:value>Quincy</xf:value>\r\n" +
			"      </xf:item>\r\n" +
			"      <xf:item>\r\n" +
			"        <xf:label>Brookline</xf:label>\r\n" +
			"        <xf:value>Brookline</xf:value>\r\n" +
			"      </xf:item>\r\n" +
			"    </xf:select1>\r\n" +
			"  </body>\r\n" +
			"</xf:html>" +
			"").getBytes(); //



	public Form getXFormObject()
	{
		DataInputStream dis = new DataInputStream(new ByteArrayInputStream(this.dimagiSurvey2));
		InputStreamReader isr = new InputStreamReader(dis);
		return XMLUtil.parseForm(isr);

	}

}
