package org.javarosa.xpath.expr;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import org.javarosa.core.util.UnavailableExternalizerException;
import org.javarosa.core.util.externalizable.ExtUtil;
import org.javarosa.core.util.externalizable.ExtWrapNullable;
import org.javarosa.core.util.externalizable.PrototypeFactory;

public class XPathQName {
	public String namespace;
	public String name;

	public XPathQName (String qname) {
		int sep = (qname == null ? -1 : qname.indexOf(":"));
		if (sep == -1) {
			init(null, qname);
		} else {
			init(qname.substring(0, sep), qname.substring(sep + 1));
		}
	}

	public XPathQName (String namespace, String name) {
		init(namespace, name);
	}

	private void init (String namespace, String name) {
		if (name == null ||
				(name != null && name.length() == 0) ||
				(namespace != null && namespace.length() == 0))
			throw new IllegalArgumentException("Invalid QName");

		this.namespace = namespace;
		this.name = name;
	}

	public String toString () {
		return (namespace == null ? name : namespace + ":" + name);
	}
	
	public void readExternal(DataInputStream in, PrototypeFactory pf)
	throws IOException, InstantiationException, IllegalAccessException,
	UnavailableExternalizerException {
		namespace = (String)ExtUtil.read(in, new ExtWrapNullable(String.class));
		name = ExtUtil.readString(in);
	}

	public void writeExternal(DataOutputStream out) throws IOException {
		ExtUtil.write(out, new ExtWrapNullable(namespace));
		ExtUtil.writeString(out, name);
	}
}
