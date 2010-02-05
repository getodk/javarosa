package org.javarosa.services.transport;

import java.io.Reader;

import org.kxml2.io.KXmlParser;
import org.kxml2.kdom.Document;
import org.xmlpull.v1.XmlPullParser;

import de.enough.polish.io.StringReader;

public class CommUtil {

	public static Document getXMLResponse (String response) {
		return getXMLResponse(new StringReader(response));
	}

	public static Document getXMLResponse (Reader reader) {
    	Document doc = new Document();
		
    	try{
    		KXmlParser parser = new KXmlParser();
    		parser.setInput(reader);
    		parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, true);
		                       
    		doc.parse(parser);
    	} catch (Exception e) {
    		System.err.println("couldn't process response payload from server!!");
    		doc = null;
    	}
			
		try {
			doc.getRootElement();
		} catch (RuntimeException re) {
			doc = null; //work around kxml bug where it should have failed to parse xml (doc == null)
						//but instead returned an empty doc that throws an exception when you try to
						//get its root element
		}
    	
    	return doc;
    }
	
}
