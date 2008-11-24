package org.javarosa.core.model.instance;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Vector;

import org.javarosa.core.model.IDataReference;
import org.javarosa.core.model.data.IAnswerData;
import org.javarosa.core.model.instance.utils.ITreeVisitor;
import org.javarosa.core.util.externalizable.DeserializationException;
import org.javarosa.core.util.externalizable.ExtUtil;
import org.javarosa.core.util.externalizable.ExtWrapList;
import org.javarosa.core.util.externalizable.ExtWrapNullable;
import org.javarosa.core.util.externalizable.ExtWrapTagged;
import org.javarosa.core.util.externalizable.ExternalizableHelperDeprecated;
import org.javarosa.core.util.externalizable.PrototypeFactory;

/**
 * QuestionDataElement is a TreeElement of a DataModelTree that is a leaf which
 * contains the answer to a QuestionDef.
 *
 * In an XML Analogy, this represents a terminal element in the XML tree.
 *
 * It is important that this question's data reference be set to a template
 * reference before attempting deserialization.
 *
 * @author Clayton Sims
 *
 */
public class QuestionDataElement extends TreeElement {

	/** The actual question data value */
	private IAnswerData value;

	/** A Binding for the Question Definition */
	private IDataReference reference;

	/**
	 * Creates a new, blank, QuestionDataElement;
	 */
	public QuestionDataElement() {
		this.root = this;
	}

	/**
	 * Creates a new QuestionDataElement for the question defined by the name
	 * and reference provided
	 *
	 * @param name
	 *            The name of this TreeElement
	 * @param reference
	 *            The reference for Question Definitions
	 */
	public QuestionDataElement(String name, IDataReference reference) {
		this();
		this.name = name;
		this.reference = reference;
	}

	/**
	 * Creates a new QuestionDataElement for the question defined by the name
	 * and reference provided, and sets its value to that provided.
	 *
	 * @param name
	 *            The name of this TreeElement
	 * @param reference
	 *            The reference for Question Definitions
	 * @param value
	 *            The value for this Question Definition
	 */
	public QuestionDataElement(String name, IDataReference reference,
			IAnswerData value) {
		this(name, reference);
		this.value = value;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.javarosa.core.model.TreeElement#getName()
	 */
	public String getName() {
		return name;
	}

	/**
	 * @return The value for the question defined by IBinding
	 */
	public IAnswerData getValue() {
		return value;
	}

	/**
	 * Sets the value for the question defined by IBinding
	 *
	 * @param value
	 *            The question's answer value
	 */
	public void setValue(IAnswerData value) {
		this.value = value;
	}

	public void setName(String name) {
		this.name = name;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.javarosa.core.model.TreeElement#isLeaf()
	 */
	public boolean isLeaf() {
		return true;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.javarosa.core.model.TreeElement#setRoot(org.javarosa.core.model.TreeElement)
	 */
	protected void setRoot(TreeElement root) {
		this.root = root;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.javarosa.core.model.TreeElement#contains(org.javarosa.core.model.TreeElement)
	 */
	public boolean contains(TreeElement element) {
		if (this == element) {
			return true;
		} else {
			return false;
		}
	}

	public void setReference(IDataReference reference) {
		this.reference = reference;
	}

	public boolean matchesReference(IDataReference reference) {
		if (this.reference == null) {
			return false;
		} else {
			return this.reference.referenceMatches(reference);
		}
	}

	/**
	 * @return a string representing the value of this question's answer
	 */
	public String createStringValue() {
		return value.getDisplayText();
	}

	/**
	 * Visitor pattern acceptance method.
	 *
	 * @param visitor
	 *            The visitor traveling this tree
	 */
	public void accept(ITreeVisitor visitor) {
		visitor.visit(this);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.javarosa.core.services.storage.utilities.Externalizable#readExternal(java.io.DataInputStream)
	 */
	public void readExternal(DataInputStream in, PrototypeFactory pf) throws IOException, DeserializationException {
		name = ExtUtil.readString(in);
		reference = (IDataReference)ExtUtil.read(in, new ExtWrapTagged(), pf);
		
		// read attributes
		Vector attStrings = ExtUtil.nullIfEmpty((Vector)ExtUtil.read(in, new ExtWrapList(String.class), pf));
		setAttributesFromSingleStringVector(attStrings);

		value = (IAnswerData)ExtUtil.read(in, new ExtWrapNullable(new ExtWrapTagged()), pf);	
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.javarosa.core.services.storage.utilities.Externalizable#writeExternal(java.io.DataOutputStream)
	 */
	public void writeExternal(DataOutputStream out) throws IOException {
		ExtUtil.writeString(out, name);
		ExtUtil.write(out, new ExtWrapTagged(reference));

		// write attributes.
		// 1. Add attributes to vector as full strings  'qid="82"'
		// 2. @ writeExternal - call ExternalizableHelperDeprecated.writeUTFs to write the vector
		// 3. @ readExternal read from vector
		// 4. Don't forget you'll need to externalize them in XMLserializer too. :-(
		Vector attStrings = getSingleStringAttributeVector();
		ExtUtil.write(out, new ExtWrapList(ExtUtil.emptyIfNull(attStrings)));

		ExtUtil.write(out, new ExtWrapNullable(value == null ? null : new ExtWrapTagged(value)));
	}

	public IDataReference getReference(){
		return this.reference;
	}
}
