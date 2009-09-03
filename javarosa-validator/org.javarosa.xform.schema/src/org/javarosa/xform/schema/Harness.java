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

package org.javarosa.xform.schema;

import java.io.IOException;
import java.io.PrintStream;

import org.javarosa.core.model.FormDef;
import org.javarosa.xform.util.XFormUtils;
import org.kxml2.io.KXmlSerializer;
import org.kxml2.kdom.Document;

public class Harness {
	public static final int MODE_SCHEMA = 1;
	public static final int MODE_SUMMARY_TEXT = 2;
	public static final int MODE_SUMMARY_SPREADSHEET = 3;
	
	public static void main(String[] args) {
		int mode = -1;
		
		if (args.length == 0) {
			mode = MODE_SCHEMA;
		} else if (args[0].equals("schema")) {
			mode = MODE_SCHEMA;
		} else if (args[0].equals("summary")) {
			mode = MODE_SUMMARY_TEXT;
		} else {
			System.out.println("Usage: java -jar form_translate.jar [schema|summary] < form.xml > output");
			System.exit(1);
		}
		
		PrintStream sysOut = System.out;
		System.setOut(System.err);
		FormDef f = XFormUtils.getFormFromInputStream(System.in);
		System.setOut(sysOut);
		
		if (mode == MODE_SCHEMA) {			
			Document schemaDoc = InstanceSchema.generateInstanceSchema(f);
			KXmlSerializer serializer = new KXmlSerializer();
			try {
				serializer.setOutput(System.out, null);
				schemaDoc.write(serializer);
				serializer.flush();
			} catch (IOException e) {
				e.printStackTrace();
			}
		} else if (mode == MODE_SUMMARY_TEXT) {
			System.out.println(FormOverview.overview(f));
		}
	}
}
