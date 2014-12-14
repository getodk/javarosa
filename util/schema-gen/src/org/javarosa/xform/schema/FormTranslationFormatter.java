/**
 *
 */
package org.javarosa.xform.schema;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.StringWriter;
import java.nio.charset.Charset;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.TreeMap;
import java.util.Vector;

import org.javarosa.core.model.FormDef;
import org.javarosa.core.services.locale.Localizer;
import org.javarosa.core.util.OrderedMap;
import org.javarosa.xpath.XPathConditional;
import org.kxml2.kdom.Document;
import org.kxml2.kdom.Element;
import org.xmlpull.mxp1_serializer.MXSerializer;

import com.csvreader.CsvReader;
import com.csvreader.CsvWriter;

/**
 * @author ctsims
 *
 */
public class FormTranslationFormatter {

	public static StringBuilder dumpTranslationsIntoCSV(FormDef f) {
		//absorb errors.
		return dumpTranslationsIntoCSV(f, new StringBuilder());
	}

	public static StringBuilder dumpTranslationsIntoCSV(FormDef f, StringBuilder messages) {
		f.getLocalizer().setToDefault();

		HashMap<String,OrderedMap> localeData = new HashMap<String,OrderedMap>();
		HashMap<Integer, String[]> techStrings = new HashMap<Integer, String[]>();

		StringWriter writer = new StringWriter();

		CsvWriter csv = new CsvWriter(writer, ',');

		String[] locales = f.getLocalizer().getAvailableLocales();
		String[] header = new String[locales.length + 1];
		header[0] = "id";
		for(int i = 0 ; i < locales.length ; ++ i) {
			header[i+1] = locales[i];
			localeData.put(locales[i],f.getLocalizer().getLocaleMap(locales[i]));
		}

		try {
			csv.writeRecord(header);
		} catch (IOException e) {
			messages.append("Error!" + e.getMessage());
		}

		OrderedMap defaultLocales = localeData.get(f.getLocalizer().getLocale());
		//Go through the keys for the default translation, there should be a one-to-one mapping between
		//each set of available keys.
		for ( Object okey : defaultLocales.keySet() ) {
			String key = (String) okey;
			String[] rowOfTranslations = new String[locales.length + 1];
			rowOfTranslations[0] = key;
			int index = 1;
			//Now dump the translation for each key per-language
			for(String locale : locales) {
				String translation = (String)localeData.get(locale).get(key);
				rowOfTranslations[index] = translation;

				Vector<String> arguments = (Vector<String>) Localizer.getArgs(translation);
				for (String arg : arguments) {
					try {
						int nArg = Integer.parseInt(arg);
						XPathConditional expr = (XPathConditional) f.getOutputFragments().elementAt((nArg));
						//println(sb, indent + 1, expr.xpath);
						if(!techStrings.containsKey(Integer.valueOf(nArg))) {
							techStrings.put(Integer.valueOf(nArg),new String[locales.length + 1]);
						}
						techStrings.get(Integer.valueOf(nArg))[index] = expr.xpath;

					} catch (NumberFormatException e) {
						messages.append("Error!" + e.getMessage());
						e.printStackTrace();
					}
				}
				index++;
			}
			try {
				csv.writeRecord(rowOfTranslations);
			} catch (IOException e) {
				messages.append("Error!" + e.getMessage());
			}
		}

		for(Integer nArg : techStrings.keySet()) {
			String[] record = techStrings.get(nArg);
			record[0] = nArg.toString();
			try {
				csv.writeRecord(record);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		//output.append(writer.getBuffer());
		return new StringBuilder(writer.toString());
	}

	public static void turnTranslationsCSVtoItext(InputStream stream, OutputStream output, String delimeter, String encoding, String printEncoding) {
		InputStreamReader reader;
		if(encoding == null) {
			reader = new InputStreamReader(stream);
		} else {
			Charset charset = Charset.forName(encoding);
			reader = new InputStreamReader(stream, charset);
		}

		//Lots of Dictionaries!
		//Treemap is important here to keep ordering constraints.
		TreeMap<String,Element> itexts = new TreeMap<String,Element>();
		HashMap<String,HashMap<String,Element>> textValues = new HashMap<String,HashMap<String,Element>>();
		HashMap<String, HashMap<String,String>> args = new HashMap<String, HashMap<String,String>>();

		CsvReader csv;

		if(delimeter == null) {
			csv = new CsvReader(reader);
		} else {
			if(delimeter.equals("\\t")) {
				csv = new CsvReader(reader,'\t');
			} else if(delimeter.length() > 1) {
				System.err.println("Invalid delimeter: " + delimeter + " Using comma");
				csv = new CsvReader(reader);
			} else {
				csv = new CsvReader(reader,delimeter.charAt(0));
			}
		}

		Document doc = new Document();
		try {
			csv.readHeaders();
			String[] headers = csv.getHeaders();
			for(int i = 1 ; i < headers.length; ++i) {
				Element translation = doc.createElement(null,"translation");
				translation.setAttribute(null, "lang",headers[i]);
				itexts.put(headers[i], translation);
				textValues.put(headers[i], new HashMap<String,Element>());
				args.put(headers[i], new HashMap<String,String>());
			}
			while(csv.readRecord()) {
				String[] values = csv.getValues();
				String id = values[0];
				String form = null;


				//If the id is an integer, it's an argument, not a text element
				try {
					int arg = Integer.parseInt(id);
					for(int i = 1; i < values.length ; i++) {
						args.get(headers[i]).put(id, "<output value=\"" +  values[i] + "\"/>");
					}
					continue;
				}
				catch(NumberFormatException e) {
					//Java is so stupid, I still can't believe this is how you
					//check whether a string is an integer....

					//Don't do anything here, it's an expected outcome.
				}

				//Do this outside of the try catch to get out of the exception handling part of the vm
				//We won't get here if the id was an arg, since it gets continued.
				if(id.contains((";"))) {
					//Sketchy! but it's sketchy in the parser, too...
					String[] divided = id.split(";");
					id = divided[0];
					form = divided[1];
				}
				int read = Math.min(headers.length,values.length);
				for(int i = 1 ; i < read ; i++) {
					String valueText = values[i];
					Element text;

					//Figure out whether this element exists...
					if(textValues.get(headers[i]).containsKey(id)) {
						text = textValues.get(headers[i]).get(id);
					} else {
						text = doc.createElement(null,"text");
						text.setAttribute(null,"id",id);
						textValues.get(headers[i]).put(id,text);
						itexts.get(headers[i]).addChild(Element.ELEMENT,text);
					}

					Element value = doc.createElement(null,"value");
					if(form != null) {
						value.setAttribute(null,"form",form);
					}
					value.addChild(Element.TEXT,valueText);

					text.addChild(Element.ELEMENT,value);
				}
			}

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		//Now it's time to update all of the arguments!
		for(String localeID : textValues.keySet()) {
			HashMap<String,Element> localeValueElements = textValues.get(localeID);
			for(Element textElement : localeValueElements.values()) {
				for(int i = 0 ; i < textElement.getChildCount(); ++i ){
					if(textElement.getChild(i) instanceof Element) {
						Element valueElement = (Element)textElement.getChild(i);
						if(valueElement.getName().equals("value")) {
							//Now we're at a value element, it should have one, and only one, child
							//element which contains text that may or may not have an argument string
							//that needs to be updated.
							String processedString = Localizer.processArguments((String)valueElement.getChild(0), args.get(localeID));
							valueElement.removeChild(0);
							valueElement.addChild(Element.IGNORABLE_WHITESPACE, processedString);

						}
					}
				}
			}
		}

		Element itext = doc.createElement(null, "itext");
		for(Element el : itexts.values()) {
			itext.addChild(Document.ELEMENT, el);
		}

		doc.addChild(Document.ELEMENT, itext);


		MXSerializer ser = new MXSerializer();

		ser.setProperty("http://xmlpull.org/v1/doc/properties.html#serializer-indentation", "    ");
		ser.setProperty("http://xmlpull.org/v1/doc/properties.html#serializer-line-separator", "\n");

		try {

		if(encoding == null) {
			StringWriter writer = new StringWriter();
			ser.setOutput(writer);

			doc.write(ser);

			StringBuilder outputBuffer = new StringBuilder();
			outputBuffer.append(writer);
			PrintStream p = new PrintStream(output);
			p.println(outputBuffer);
		} else if(printEncoding == null){
			ser.setOutput(output, encoding);

			doc.write(ser);
		} else {
			ser.setOutput(output, printEncoding);

			doc.write(ser);
		}


		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			throw new RuntimeException(e);
		}
	}

	public static void turnTranslationsCSVtoItext(InputStream stream, OutputStream output) {
		turnTranslationsCSVtoItext(stream, output, null, null, null);
	}

}
