/*
 * Copyright (C) 2009 JavaRosa
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

package org.javarosa.test.sampleforms;

public class SampleForms {
	public static byte[] getImmunizationWidgetForm() {
		return  new String("" +
				"<h:html xmlns:h='http://www.w3.org/1999/xhtml' xmlns='http://www.w3.org/2002/xforms' xmlns:ev='http://www.w3.org/2001/xml-events' xmlns:xsd='http://www.w3.org/2001/XMLSchema' xmlns:jr='http://openrosa.org/javarosa'>\r\n" +
				"  <h:head>\r\n" +
				"    <h:title>Title</h:title>\r\n" +
				"    <model id=\"DimagiSurvey\">\r\n" +
				"      <instance>\r\n" +
				"        <data>\r\n" +
				"        </data>\r\n" +
				"      <instance>\r\n" +
				"    <model>\r\n" +
				"  </h:head>\r\n" +
				"  <h:body>\r\n" +
				"    <table><label>Immunization Table</label></table>\r\n" + 
				"  </h:body>\r\n" +
				"</h:html>" +
				"").getBytes();
	}
	public static byte[] getGraphWidgetForm() {
		return  new String("" +
				"<h:html xmlns:h='http://www.w3.org/1999/xhtml' xmlns='http://www.w3.org/2002/xforms' xmlns:ev='http://www.w3.org/2001/xml-events' xmlns:xsd='http://www.w3.org/2001/XMLSchema' xmlns:jr='http://openrosa.org/javarosa'>\r\n" +
				"  <h:head>\r\n" +
				"    <h:title>Title</h:title>\r\n" +
				"    <model id=\"DimagiSurvey\">\r\n" +
				"      <instance>\r\n" +
				"        <data>\r\n" +
				"        </data>\r\n" +
				"      <instance>\r\n" +
				"    <model>\r\n" +
				"  </h:head>\r\n" +
				"  <h:body>\r\n" +
				" <graph>\r\n" + 
				"  <label>Graph</label>\r\n" +
				"  <hint>Graph</hint>\r\n" +
				" </graph>" +
				"  </h:body>\r\n" +
				"</h:html>" +
				"").getBytes();
	}
}
