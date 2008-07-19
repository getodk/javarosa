package org.javarosa.core.model.instance;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Enumeration;

import org.javarosa.core.model.IDataReference;
import org.javarosa.core.model.IFormDataModel;
import org.javarosa.core.model.data.IAnswerData;
import org.javarosa.core.model.instance.utils.ExternalizingVisitor;
import org.javarosa.core.model.instance.utils.ITreeVisitor;
import org.javarosa.core.model.utils.ExternalizableHelper;
import org.javarosa.core.model.utils.IDataModelVisitor;
import org.javarosa.core.services.storage.utilities.UnavailableExternalizerException;

/**
 * DataModelTree is an implementation of IFormDataModel
 * that contains a Data Model which stores Question Answers
 * in an XML-style hierarchical tree, with no repeated
 * tree elements.
 *  
 * @author Clayton Sims
 *
 */
public class DataModelTree implements IFormDataModel {

	/** The root of this tree */
	private TreeElement root;
	
	/** The name for this data model */
	private String name;
	
	/** The integer Id of the model */
	private int id;
	
	public DataModelTree() { 
	}
	
	/**
	 * Creates a new data model using the root given.
	 * 
	 * @param root The root of the tree for this data model.
	 */
	public DataModelTree(TreeElement root) {
		this.root = root;
	}
	
	/**
	 * Sets the root element of this Model's tree
	 * @param root The root of the tree for this data model.
	 */
	public void setRootElement(TreeElement root) {
		this.root = root;
	}
	
	/**
	 * @return This model's root tree element
	 */
	public TreeElement getRootElement() {
		return root;
	}

	public DataOutputStream externalizeToXMLInstance() {
		// TODO Auto-generated method stub
		return null;
	}
	/*
	 * (non-Javadoc)
	 * @see org.javarosa.core.model.IFormDataModel#updateDataValue(IDataBinding, Object)
	 */
	public boolean updateDataValue(IDataReference questionBinding, IAnswerData value) {
		QuestionDataElement questionElement = resolveReference(questionBinding);
		if(questionElement != null) {
			questionElement.setValue(value);
			return true;
		}
		else {
			return false;
		}
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.javarosa.core.model.IFormDataModel#getDataValue(org.javarosa.core.model.IDataReference)
	 */
	public IAnswerData getDataValue(IDataReference questionReference) {
		QuestionDataElement element = resolveReference(questionReference);
		if(element != null) {
			return element.getValue();
		}
		else {
			return null;
		}
	}

	
	/**
	 * Resolves a binding to a particular question data element
	 * @param binding The binding representing a particular question
	 * @return A QuestionDataElement corresponding to the binding
	 * provided. Null if none exists in this tree.
	 */
	public QuestionDataElement resolveReference(IDataReference binding) {
		if (root.isLeaf()) {
			if ((root.getClass() == QuestionDataElement.class)
					&& ((QuestionDataElement) root).matchesReference(binding)) {
				return (QuestionDataElement) root;
			} else {
				return null;
			}
		} else {
			return resolveReference(binding,(QuestionDataGroup)root);
		}
	}
	
	/**
	 * Resolves a binding to a particular question data element
	 * @param binding The binding representing a particular question
	 * @param group 
	 * @return A QuestionDataElement corresponding to the binding
	 * provided. Null if none exists in this tree.
	 */
	private QuestionDataElement resolveReference(IDataReference binding, QuestionDataGroup group) {
		Enumeration en = group.getChildren().elements();
		while(en.hasMoreElements()) {
			TreeElement dme = (TreeElement)en.nextElement();
			if(!dme.isLeaf()) {
				return resolveReference(binding, (QuestionDataGroup)dme);
			} else {
				if ((root.getClass() == QuestionDataElement.class)
						&& ((QuestionDataElement) dme).matchesReference(binding)) {
					return (QuestionDataElement) dme;
				}
			}
		}
		return null;
	}
	
	/**
	 * Identifies whether the tree for this DataModel contains the given element.
	 * 
	 * @param element The element to be identified
	 * @return True if this model's tree contains the given element. False otherwise.
	 */
	public boolean contains(TreeElement element) {
		return root.contains(element);
	}
	
	public void accept(IDataModelVisitor visitor) {
		visitor.visit(this);
		if(root != null) {
			//I don't think this is going to work.
			//if(visitor.getClass() == ITreeVisitor.class) {
			if(visitor instanceof ITreeVisitor) {
				root.accept((ITreeVisitor)visitor);
			}
		}
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.javarosa.core.services.storage.utilities.Externalizable#readExternal(java.io.DataInputStream)
	 */
	public void readExternal(DataInputStream in) throws IOException,
			InstantiationException, IllegalAccessException, UnavailableExternalizerException {
		this.id = in.readInt();
		this.name = ExternalizableHelper.readUTF(in);
		boolean group = in.readBoolean();
		if(group) {
			QuestionDataGroup newGroup = new QuestionDataGroup();
			newGroup.setRoot(newGroup);
			newGroup.readExternal(in);
		}
		else {
			QuestionDataElement element = new QuestionDataElement();
			element.setRoot(element);
			element.readExternal(in);
		}
	}

	/*
	 * (non-Javadoc)
	 * @see org.javarosa.core.services.storage.utilities.Externalizable#writeExternal(java.io.DataOutputStream)
	 */
	public void writeExternal(DataOutputStream out) throws IOException {
		out.writeInt(this.id);
		ExternalizableHelper.writeUTF(out, this.name);
		ExternalizingVisitor visitor = new ExternalizingVisitor(out);
		visitor.visit(this);
	}

	/*
	 * (non-Javadoc)
	 * @see org.javarosa.core.model.IFormDataModel#getName()
	 */
	public String getName() {
		return name;
	}

	/**
	 * Sets the name of this datamodel instance
	 * @param name The name to be used
	 */
	public void setName(String name) {
		this.name = name;
	}

	/*
	 * (non-Javadoc)
	 * @see org.javarosa.core.model.IFormDataModel#getId()
	 */
	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}
}
