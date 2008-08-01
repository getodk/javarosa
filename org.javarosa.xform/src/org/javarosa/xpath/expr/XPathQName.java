package org.javarosa.xpath.expr;

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
}
