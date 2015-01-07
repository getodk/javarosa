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

import java.io.FileInputStream;
import java.io.FileNotFoundException;
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
	public static final int MODE_CSV_DUMP = 4;
	public static final int MODE_CSV_IMPORT = 5;
	public static final int MODE_VALIDATE_MODEL = 6;
	
	public static void main(String[] args) {
		int mode = -1;
		
		if (args.length == 0) {
			mode = MODE_SCHEMA;
		} else if (args[0].equals("schema")) {
			mode = MODE_SCHEMA;
		} else if (args[0].equals("summary")) {
			mode = MODE_SUMMARY_TEXT;
		} else if (args[0].equals("csvdump")) {
			mode = MODE_CSV_DUMP;
		} else if (args[0].equals("csvimport")) {
			mode = MODE_CSV_IMPORT;
		} else if (args[0].equals("validatemodel")) {
			mode = MODE_VALIDATE_MODEL;
		} else {
			System.out.println("Usage: java -jar form_translate.jar [schema|summary|csvdump] < form.xml > output");
			System.out.println("or: java -jar form_translate.jar csvimport [delimeter] [encoding] [outcoding] < translations.csv > itextoutput");
			System.out.println("or: java -jar form_translate.jar validatemodel /path/to/xform /path/to/instance");
			System.exit(1);
		}
		
		if(mode == MODE_VALIDATE_MODEL) {
			
			String formPath = args[1];
			String modelPath = args[2];
			
			FileInputStream formInput = null;
			FileInputStream instanceInput = null;
			
			try {
				formInput = new FileInputStream(formPath);
			} catch (FileNotFoundException e) {
				System.out.println("Couldn't find file at: " + formPath);
				System.exit(1);
			}
			
			try {
				instanceInput = new FileInputStream(modelPath);
			} catch (FileNotFoundException e) {
				System.out.println("Couldn't find file at: " + modelPath);
				System.exit(1);
			}
			
			try {
				FormInstanceValidator validator = new FormInstanceValidator(formInput, instanceInput);
				validator.simulateEntryTest();
			} catch (Exception e) {
				e.printStackTrace();
				System.exit(1);
			}
			
			System.out.println("Form instance appears to be valid");
			System.exit(0);
		}
		
		
		PrintStream sysOut = System.out;
		System.setOut(System.err);
		
		if(mode == MODE_CSV_IMPORT) {
			System.setOut(sysOut);
			if(args.length > 1) {
				String delimeter = args[1];
				FormTranslationFormatter.turnTranslationsCSVtoItext(System.in, System.out, delimeter, null,null);
			}
			else if(args.length > 2) {
				String delimeter = args[1];
				String encoding = args[2];
				FormTranslationFormatter.turnTranslationsCSVtoItext(System.in, System.out, delimeter, encoding, null);
			} else if(args.length > 3) {
				String delimeter = args[1];
				String incoding = args[2];
				String outcoding = args[3];
				FormTranslationFormatter.turnTranslationsCSVtoItext(System.in, System.out, delimeter, incoding, outcoding );
			} else {
				FormTranslationFormatter.turnTranslationsCSVtoItext(System.in, System.out);
			}
			System.exit(0);
		}
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
		} else if (mode == MODE_CSV_DUMP) {
			System.out.println(FormTranslationFormatter.dumpTranslationsIntoCSV(f));
		}
	}
}
