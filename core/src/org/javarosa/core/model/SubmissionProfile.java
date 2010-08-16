/**
 * 
 */
package org.javarosa.core.model;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import org.javarosa.core.util.externalizable.DeserializationException;
import org.javarosa.core.util.externalizable.ExtUtil;
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
	
	public SubmissionProfile() {
		
	}
	
	public SubmissionProfile(IDataReference ref, String method, String action, String mediatype) {
		this.method = method;
		this.ref = ref;
		this.action = action;
		this.mediaType = mediatype;
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

	@Override
	public void readExternal(DataInputStream in, PrototypeFactory pf) throws IOException, DeserializationException {
		ref = (IDataReference)ExtUtil.read(in, new ExtWrapTagged(IDataReference.class));
		method = ExtUtil.readString(in);
		action = ExtUtil.readString(in);
		mediaType = ExtUtil.nullIfEmpty(ExtUtil.readString(in));
	}

	@Override
	public void writeExternal(DataOutputStream out) throws IOException {
		ExtUtil.write(out, new ExtWrapTagged(ref));
		ExtUtil.writeString(out, method);
		ExtUtil.writeString(out, action);
		ExtUtil.writeString(out, ExtUtil.emptyIfNull(mediaType));
	}

	
}
