package org.javarosa.core.util.test;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import org.javarosa.core.util.Externalizable;
import org.javarosa.core.util.UnavailableExternalizerException;
import org.javarosa.core.util.externalizable.ExtUtil;

public class SampleExtz implements Externalizable {
	String a, b;

	public SampleExtz (String a, String b) {
		this.a = a;
		this.b = b;
	}

	public SampleExtz () {
		this("", "");
	}

	public void readExternal(DataInputStream in) throws IOException,
	InstantiationException, IllegalAccessException,
	UnavailableExternalizerException {
		a = ExtUtil.readString(in);
		b = ExtUtil.readString(in);
	}

	public void writeExternal(DataOutputStream out) throws IOException {
		ExtUtil.writeString(out, a);
		ExtUtil.writeString(out, b);			
	}

	public String toString () {
		return a + "," + b;
	}
}
