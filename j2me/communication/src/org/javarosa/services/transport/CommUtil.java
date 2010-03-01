package org.javarosa.services.transport;

import java.io.ByteArrayInputStream;
import java.io.InputStreamReader;
import java.io.Reader;

import org.kxml2.io.KXmlParser;
import org.kxml2.kdom.Document;
import org.xmlpull.v1.XmlPullParser;

import de.enough.polish.io.StringReader;

/**
 * utility class for extracting an xml document out of a string or bytestream, typically used for
 * processing xml payloads sent in response to server requests
 * 
 * @author Drew Roos
 *
 */
public class CommUtil {

	public static Document getXMLResponse (byte[] response) {
		return getXMLResponse(new InputStreamReader(new ByteArrayInputStream(response)));
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
