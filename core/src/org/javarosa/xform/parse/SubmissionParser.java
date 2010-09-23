/**
 * 
 */
package org.javarosa.xform.parse;


import org.javarosa.core.model.IDataReference;
import org.javarosa.core.model.SubmissionProfile;
import org.kxml2.kdom.Element;

/**
 * A Submission Profile 
 * 
 * @author ctsims
 *
 */
public class SubmissionParser {
	
	public SubmissionProfile parseSubmission(String method, String action, IDataReference ref, Element element) {
		String mediatype = element.getAttributeValue(null,"mediatype");
		
		return new SubmissionProfile(ref, method, action, mediatype);
	}
	
	public boolean matchesCustomMethod(String method) {
		 return false;
	}
}
