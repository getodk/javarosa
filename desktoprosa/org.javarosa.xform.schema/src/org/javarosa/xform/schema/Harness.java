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
