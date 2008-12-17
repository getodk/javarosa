/**
 * 
 */
package org.javarosa.model.xform;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import org.javarosa.core.model.IDataReference;
import org.javarosa.core.model.instance.TreeReference;
import org.javarosa.core.util.externalizable.DeserializationException;
import org.javarosa.core.util.externalizable.ExtUtil;
import org.javarosa.core.util.externalizable.PrototypeFactory;
import org.javarosa.xpath.XPathParseTool;
import org.javarosa.xpath.XPathUnsupportedException;
import org.javarosa.xpath.expr.XPathExpression;
import org.javarosa.xpath.expr.XPathPathExpr;
import org.javarosa.xpath.expr.XPathStep;
import org.javarosa.xpath.parser.XPathSyntaxException;

/**
 * 
 */
public class XPathReference implements IDataReference {
	private TreeReference ref;	
	private String nodeset;
	
	public XPathReference () {

	}
	
	public XPathReference (String nodeset) {
		XPathExpression path;
		
		try {
			
		path = XPathParseTool.parseXPath(nodeset);
		if (!(path instanceof XPathPathExpr)) {
			throw new XPathSyntaxException();
		}
		
		} catch (XPathSyntaxException xse) {
			throw new RuntimeException(); //TODO: check me
		}
		
		ref = ((XPathPathExpr)path).getReference();
		this.nodeset = nodeset;
	}
	
	public XPathReference (XPathPathExpr path) {
		ref = path.getReference();
	}

	public XPathReference (TreeReference ref) {
		this.ref = ref;
	}
	
	public Object getReference () {
		return ref;
	}
	
	public void setReference (Object o) {
		//do nothing
	}
		
	public boolean equals (Object o) {
		if (o instanceof XPathReference) {
			return ref.equals(((XPathReference)o).ref);
		} else {
			return false;
		}
	}
	
	public int hashCode () {
		return ref.hashCode();
	}

	/* (non-Javadoc)
	 * @see org.javarosa.core.services.storage.utilities.Externalizable#readExternal(java.io.DataInputStream)
	 */
	public void readExternal(DataInputStream in, PrototypeFactory pf) throws IOException, DeserializationException {
		nodeset = ExtUtil.nullIfEmpty(ExtUtil.readString(in));
		ref = (TreeReference)ExtUtil.read(in, TreeReference.class, pf);
	}

	/* (non-Javadoc)
	 * @see org.javarosa.core.services.storage.utilities.Externalizable#writeExternal(java.io.DataOutputStream)
	 */
	public void writeExternal(DataOutputStream out) throws IOException {
		ExtUtil.writeString(out, ExtUtil.emptyIfNull(nodeset));
		ExtUtil.write(out, ref);
	}
}
