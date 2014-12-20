/**
 *
 */
package org.javarosa.core.model;

import org.javarosa.core.util.externalizable.*;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.HashMap;

/**
 * A Submission Profile is a class which is responsible for
 * holding and processing the details of how a submission
 * should be handled.
 *
 * @author ctsims
 *
 */
public class SubmissionProfile implements Externalizable {

	IDataReference ref;
	String method;
	String action;
	String mediaType;
	HashMap<String,String> attributeMap;

	public SubmissionProfile() {

	}

	public SubmissionProfile(IDataReference ref, String method, String action, String mediatype) {
		this(ref, method, action, mediatype, new HashMap<String, String>());
	}

	public SubmissionProfile(IDataReference ref, String method, String action, String mediatype, HashMap<String,String> attributeMap) {
		this.method = method;
		this.ref = ref;
		this.action = action;
		this.mediaType = mediatype;
		this.attributeMap = attributeMap;
	}

	public IDataReference getRef() {
		return ref;
	}

	public String getMethod() {
		return method;
	}

	public String getAction() {
		return action;
	}

	public String getMediaType() {
		return mediaType;
	}

	public String getAttribute(String name) {
	    return attributeMap.get(name);
	}

	@SuppressWarnings("unchecked")
	public void readExternal(DataInputStream in, PrototypeFactory pf) throws IOException, DeserializationException {
		ref = (IDataReference)ExtUtil.read(in, new ExtWrapTagged(IDataReference.class));
		method = ExtUtil.readString(in);
		action = ExtUtil.nullIfEmpty(ExtUtil.readString(in));
		mediaType = ExtUtil.nullIfEmpty(ExtUtil.readString(in));
		attributeMap = (HashMap<String, String>)ExtUtil.read(in, new ExtWrapMap(String.class, String.class));
	}

	public void writeExternal(DataOutputStream out) throws IOException {
		ExtUtil.write(out, new ExtWrapTagged(ref));
		ExtUtil.writeString(out, method);
		ExtUtil.writeString(out, ExtUtil.emptyIfNull(action));
		ExtUtil.writeString(out, ExtUtil.emptyIfNull(mediaType));
		ExtUtil.write(out, new ExtWrapMap(attributeMap));
	}


}
