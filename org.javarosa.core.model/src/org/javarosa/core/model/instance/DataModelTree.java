package org.javarosa.core.model.instance;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Date;
import java.util.Enumeration;

import org.javarosa.core.JavaRosaServiceProvider;
import org.javarosa.core.model.IDataReference;
import org.javarosa.core.model.IFormDataModel;
import org.javarosa.core.model.data.IAnswerData;
import org.javarosa.core.model.instance.utils.ExternalizingVisitor;
import org.javarosa.core.model.instance.utils.ITreeVisitor;
import org.javarosa.core.model.storage.FormDefRMSUtility;
import org.javarosa.core.model.utils.ExternalizableHelper;
import org.javarosa.core.model.utils.IDataModelVisitor;
import org.javarosa.core.model.utils.PrototypeFactory;
import org.javarosa.core.util.UnavailableExternalizerException;
import org.javarosa.core.services.storage.utilities.IDRecordable;

/**
 * DataModelTree is an implementation of IFormDataModel
 * that contains a Data Model which stores Question Answers
 * in an XML-style hierarchical tree, with no repeated
 * tree elements.
 *  
 * @author Clayton Sims
 *
 */
public class DataModelTree implements IFormDataModel, IDRecordable {

	/** The root of this tree */
	private TreeElement root;
	
	/** The name for this data model */
	private String name;
	
	/** The integer Id of the model */
	private int id;
	
	/** The ID of the form that this is a model for */
	private int formIdReference;
	
	/** The date that this model was taken and recorded */
	private Date dateSaved;
	
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
	 * 
	 * @see org.javarosa.core.model.IFormDataModel#updateDataValue(IDataBinding,
	 * Object)
	 */
	public boolean updateDataValue(IDataReference questionBinding,
			IAnswerData value) {
		TreeElement treeElement = resolveReference(questionBinding);
		if (treeElement != null) {
			treeElement.setValue(value);
			return true;
		} else {
			return false;
		}
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.javarosa.core.model.IFormDataModel#getDataValue(org.javarosa.core.model.IDataReference)
	 */
	public IAnswerData getDataValue(IDataReference questionReference) {
		TreeElement element = resolveReference(questionReference);
		if(element != null) {
			return element.getValue();
		}
		else {
			return null;
		}
	}

	
	/**
	 * Resolves a binding to a particular question data element
	 * 
	 * @param binding The binding representing a particular question
	 * @return A QuestionDataElement corresponding to the binding
	 * provided. Null if none exists in this tree.
	 */
	public TreeElement resolveReference(IDataReference binding) {
		if (root.isLeaf()) {
			if (root.matchesReference(binding)) {
				return root;
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
	private TreeElement resolveReference(IDataReference binding, QuestionDataGroup group) {
		//TODO: We can do this much more intelligently given that we know the format of the bindings
		TreeElement target = null;
		
		Enumeration en = group.getChildren().elements();		
		while(target == null && en.hasMoreElements()) {
			TreeElement dme = (TreeElement)en.nextElement();			
			if (dme.matchesReference(binding)) {
				return dme;
			}
			else if(!dme.isLeaf()) {
				target = resolveReference(binding, (QuestionDataGroup)dme);
			}
		}

		return target;
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
			if(visitor instanceof ITreeVisitor) {
				root.accept((ITreeVisitor)visitor);
			}
		}
	}
	
	public void setDateSaved(Date dateSaved) {
		this.dateSaved = dateSaved;
	}
	
	public void setFormReferenceId(int formIdReference) {
		this.formIdReference = formIdReference;
	}
	
	/* (non-Javadoc)
	 * @see org.javarosa.core.model.IFormDataModel#getDateSaved()
	 */
	public Date getDateSaved() {
		return this.dateSaved;
	}

	/* (non-Javadoc)
	 * @see org.javarosa.core.model.IFormDataModel#getFormReferenceId()
	 */
	public int getFormReferenceId() {
		return this.formIdReference;
	}

	/*
	 * (non-Javadoc)
	 * @see org.javarosa.core.services.storage.utilities.Externalizable#readExternal(java.io.DataInputStream)
	 */
	public void readExternal(DataInputStream in) throws IOException,
			InstantiationException, IllegalAccessException, UnavailableExternalizerException {
		this.id = in.readInt();
		this.formIdReference = in.readInt();
		
		this.name = ExternalizableHelper.readUTF(in);
		
		this.dateSaved = ExternalizableHelper.readDate(in);
		
		FormDefRMSUtility fdrms = (FormDefRMSUtility)JavaRosaServiceProvider.instance().getStorageManager().getRMSStorageProvider().getUtility(FormDefRMSUtility.getUtilityName());
		PrototypeFactory factory = fdrms.getQuestionElementsFactory();
		factory.addNewPrototype(QuestionDataGroup.class.getName(), QuestionDataGroup.class);
		
		boolean group = in.readBoolean();
		if(group) {
			String className = in.readUTF();
			QuestionDataGroup newGroup = (QuestionDataGroup)factory.getNewInstance(className);
			if(newGroup == null) {
				throw new UnavailableExternalizerException("Attempted to deserialize a Question Data Group object" +
						"of type " + className + ". Please ensure that this class is available in the prototype factory" +
						" in the root of the data model tree"); 
			}
			newGroup.setRoot(newGroup);
			newGroup.setFactory(factory);
			newGroup.readExternal(in);
			setRootElement(newGroup);
		}
		else {
			QuestionDataElement element = new QuestionDataElement();
			element.setRoot(element);
			element.readExternal(in);
			setRootElement(element);
		}
	}

	/*
	 * (non-Javadoc)
	 * @see org.javarosa.core.services.storage.utilities.Externalizable#writeExternal(java.io.DataOutputStream)
	 */
	public void writeExternal(DataOutputStream out) throws IOException {
		out.writeInt(this.id);
		out.writeInt(this.formIdReference);
		
		ExternalizableHelper.writeUTF(out, this.name);
		
		ExternalizableHelper.writeDate(out, this.dateSaved);
		
		ExternalizingVisitor visitor = new ExternalizingVisitor(out);
		this.accept(visitor);
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
	
	//treating id and record id as the same until we resolve the need for both of them
	public int getRecordId () {
		return getId();
	}
	
	public void setRecordId(int recordId) {
		setId(recordId);
	}
}
