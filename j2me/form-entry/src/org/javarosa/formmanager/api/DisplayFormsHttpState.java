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

package org.javarosa.formmanager.api;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.List;

import org.javarosa.core.api.State;
import org.javarosa.formmanager.api.transitions.FormListTransitions;
import org.javarosa.formmanager.view.AvailableFormsScreen;
import org.javarosa.j2me.view.J2MEDisplay;
import org.kxml2.io.KXmlParser;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

public abstract class DisplayFormsHttpState implements FormListTransitions, State, CommandListener {
	private AvailableFormsScreen formList;
	private ByteArrayInputStream bin;

	private KXmlParser parser = new KXmlParser();
	private Vector items;
	private Hashtable formInfo;

	public static final String SELECTED_FORM = "selected_form";
	public static final String FORM_URL = "selected_form_url";


	public DisplayFormsHttpState(byte[] formsListXml) {
		init(formsListXml);
	}

	public void init(byte[] formsListXml){
		try{
			bin = new ByteArrayInputStream(formsListXml);

			//setup parser
			parser.setInput(new InputStreamReader(bin));

			items = new Vector();
			formInfo = new Hashtable();

			processSurveyList(parser, formInfo);
			
			String[] formlist = new String[ formInfo.size() ];
			Enumeration e = formInfo.keys();//read available form names from hasttable
	
			for(int i=0;i<formInfo.size(); i++)
		{
			items.addElement(e.nextElement());
		}
		items.copyInto( formlist );

			formList = new AvailableFormsScreen("Available Forms",formlist);
			formList.setCommandListener(this);

		}catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			System.out.println(e.getMessage());
		}

	}

	public void processSurveyList(KXmlParser parser, Hashtable formInfo) throws XmlPullParserException{
		try {
			//boolean inItem = false;
			parser.nextTag();
			parser.require(XmlPullParser.START_TAG, null, "forms");
			while( parser.nextTag() != XmlPullParser.END_TAG ){
				//parser file names
				parser.require(XmlPullParser.START_TAG, null, null);
				
				String name = parser.getName();
				String url = parser.getAttributeValue(null, "url");
				String text = parser.nextText();
								
				if(name.equals("form"))
					{
					//inItem = true;
					//items.addElement(text);
					formInfo.put(text, url);
					}
				else
					//inItem = false;

				parser.require(XmlPullParser.END_TAG, null, "form");
			}
			parser.require(XmlPullParser.END_TAG, null, "forms");
			
			parser.next();
			parser.require(XmlPullParser.END_DOCUMENT, null, null);

		} catch (IOException e) {
			// TODO: handle exception
			System.out.println("XML parser error");
			e.printStackTrace();

		}
	}

	public void start() {
		J2MEDisplay.setView(formList);
	}

	public void commandAction(Command command, Displayable display) {
		if(display == formList){
			if (command == formList.CMD_CANCEL) {
				cancel();
			}if(command == List.SELECT_COMMAND){
				String formName = formList.getString(formList.getSelectedIndex());
				String formurl = (String) formInfo.get(formName);
				this.formSelected(formurl);
			}
		}
	}
}
