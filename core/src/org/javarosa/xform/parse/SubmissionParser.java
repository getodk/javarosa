/**
 * 
 */
package org.javarosa.xform.parse;


import org.javarosa.core.model.IDataReference;
import org.javarosa.core.model.SubmissionProfile;
import org.kxml2.kdom.Element;

import java.util.HashMap;

/**
 * A Submission Profile 
 * 
 * @author ctsims
 *
 */
public class SubmissionParser {
	
    public SubmissionProfile parseSubmission(String method, String action, IDataReference ref, Element element) {
        String mediatype = element.getAttributeValue(null,"mediatype");
        HashMap<String,String> attributeMap = new HashMap<String,String>();
        int nAttr = element.getAttributeCount();
        for ( int i = 0 ; i < nAttr ; ++i ) {
            String name = element.getAttributeName(i);
            if ( name.equals("ref")) continue;
            if ( name.equals("bind")) continue;
            if ( name.equals("method")) continue;
            if ( name.equals("action")) continue;
            String value = element.getAttributeValue(i);
            attributeMap.put(name, value);
        }
        return new SubmissionProfile(ref, method, action, mediatype, attributeMap);
    }
	
	public boolean matchesCustomMethod(String method) {
		 return false;
	}
}
