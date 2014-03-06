/**
 *
 */
package org.javarosa.core.model;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Hashtable;

import org.javarosa.core.util.externalizable.DeserializationException;
import org.javarosa.core.util.externalizable.ExtUtil;
import org.javarosa.core.util.externalizable.ExtWrapMap;
import org.javarosa.core.util.externalizable.ExtWrapTagged;
import org.javarosa.core.util.externalizable.Externalizable;
import org.javarosa.core.util.externalizable.PrototypeFactory;

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
	Hashtable<String,String> attributeMap;

	public SubmissionProfile() {

	}

	public SubmissionProfile(IDataReference ref, String method, String action, String mediatype, Hashtable<String,String> attributeMap) {
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
		attributeMap = (Hashtable<String, String>)ExtUtil.read(in, new ExtWrapMap(String.class, String.class));
	}

	public void writeExternal(DataOutputStream out) throws IOException {
		ExtUtil.write(out, new ExtWrapTagged(ref));
		ExtUtil.writeString(out, method);
		ExtUtil.writeString(out, ExtUtil.emptyIfNull(action));
		ExtUtil.writeString(out, ExtUtil.emptyIfNull(mediaType));
		ExtUtil.write(out, new ExtWrapMap(attributeMap));
	}


}
