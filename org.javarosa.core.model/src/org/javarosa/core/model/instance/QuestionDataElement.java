package org.javarosa.core.model.instance;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import org.javarosa.core.model.IDataReference;
import org.javarosa.core.model.data.IAnswerData;
import org.javarosa.core.model.instance.utils.ITreeVisitor;
import org.javarosa.core.model.utils.ExternalizableHelper;
import org.javarosa.core.services.storage.utilities.UnavailableExternalizerException;
import org.javarosa.core.util.Map;

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
		return value.toString();
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
	public void readExternal(DataInputStream in) throws IOException,
			InstantiationException, IllegalAccessException,
			UnavailableExternalizerException {
		this.name = ExternalizableHelper.readUTF(in);
		String className = in.readUTF();
		reference = this.getRoot().getFactory().getNewReference(className);
		if (reference == null) {
			throw new UnavailableExternalizerException(
					"Attempt to resolve serialization for a DataModelTree failed because there was no reference " +
					"template available to deserialize the stored reference");
		}
		reference.readExternal(in);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.javarosa.core.services.storage.utilities.Externalizable#writeExternal(java.io.DataOutputStream)
	 */
	public void writeExternal(DataOutputStream out) throws IOException {
		//This flag is in place to determine whether a Data element is a Group or a Data
		//True for groups, false for DataElements
		out.writeBoolean(false);
		ExternalizableHelper.writeUTF(out, this.name);
		out.writeUTF(reference.getClass().getName());
		reference.writeExternal(out);
	}
}
