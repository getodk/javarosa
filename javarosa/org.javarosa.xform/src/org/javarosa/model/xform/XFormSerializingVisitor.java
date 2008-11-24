package org.javarosa.model.xform;

import java.io.IOException;
import java.util.Enumeration;
import java.util.Hashtable;

import org.javarosa.core.model.FormDef;
import org.javarosa.core.model.IAnswerDataSerializer;
import org.javarosa.core.model.IFormDataModel;
import org.javarosa.core.model.instance.DataModelTree;
import org.javarosa.core.model.instance.QuestionDataElement;
import org.javarosa.core.model.instance.QuestionDataGroup;
import org.javarosa.core.model.instance.TreeElement;
import org.javarosa.core.model.instance.utils.ITreeVisitor;
import org.javarosa.core.model.utils.IDataModelSerializingVisitor;
import org.javarosa.xform.util.XFormAnswerDataSerializer;
import org.javarosa.xform.util.XFormSerializer;
import org.kxml2.kdom.Document;
import org.kxml2.kdom.Element;
import org.kxml2.kdom.Node;

/**
 * A visitor class which walks a DataModelTree and constructs an XML document
 * containing its instance.
 *
 * The XML node elements are constructed in a depth-first manner, consistent with
 * standard XML document parsing.
 *
 * @author Clayton Sims
 *
 */
public class XFormSerializingVisitor implements IDataModelSerializingVisitor, ITreeVisitor {

	/** The XML document containing the instance that is to be returned */
	Document theXmlDoc;

	/** A hashtable linking TreeElements to the xml element that they should add themselves to */
	Hashtable parentList;

	/** The serializer to be used in constructing XML for AnswerData elements */
	IAnswerDataSerializer serializer;
	
	/** The schema to be used to serialize answer data */
	FormDef schema;

	
	
	/*
	 * (non-Javadoc)
	 * @see org.javarosa.core.model.utils.ITreeVisitor#visit(org.javarosa.core.model.DataModelTree)
	 */
	public void visit(DataModelTree tree) {
		theXmlDoc = new Document();
		parentList = new Hashtable();
		parentList.put(tree.getRootElement(), theXmlDoc);
	}

	/*
	 * (non-Javadoc)
	 * @see org.javarosa.core.model.utils.ITreeVisitor#visit(org.javarosa.core.model.QuestionDataElement)
	 */
	public void visit(QuestionDataElement element) {

		//First create the textual element for this question data
		Element text = new Element();
		text.setName(element.getName());
		// add attributes
		for(int i = 0; i<element.getAttributeCount();i++){
			String namespace= element.getAttributeNamespace(i);
			String name		= element.getAttributeName(i);
			String val		= element.getAttributeValue(i);
			text.setAttribute(namespace, name, val);
		}

		//(I think that the below is right. I could be very wrong, and it could
		//require us to create a new element, instead of throwing the string in)
		if(serializer == null) {
			throw new NullPointerException("No Answer Data Serializer Could be Found to Serialize Answers");
		}
		Object serializedAnswerData = serializer.serializeAnswerData(element, schema);
		if(serializedAnswerData.getClass() == String.class) {
			text.addChild(Element.TEXT, serializedAnswerData);
		} else if(serializedAnswerData instanceof Element) {
			text.addChild(Element.ELEMENT, serializedAnswerData);

		}

		//Attach to parent
		Node parentNode = (Node)parentList.get(element);

		if(parentNode != null) {
			parentNode.addChild(Element.ELEMENT, text);
		}
	}

	/*
	 * (non-Javadoc)
	 * @see org.javarosa.core.model.utils.ITreeVisitor#visit(org.javarosa.core.model.QuestionDataGroup)
	 */
	public void visit(QuestionDataGroup element) {
		Element thisNode = new Element();
		thisNode.setName(element.getName());

		Node parentNode = (Node)parentList.get(element);

		if(parentNode != null) {
			parentNode.addChild(Element.ELEMENT, thisNode);
		}

		Enumeration en = element.getChildren().elements();
		while (en.hasMoreElements()){
			parentList.put(en.nextElement(), thisNode);
		}
	}

	/*
	 * (non-Javadoc)
	 * @see org.javarosa.core.model.utils.ITreeVisitor#visit(org.javarosa.core.model.TreeElement)
	 */
	public void visit(TreeElement element) {
		// TODO Auto-generated method stub

	}

	/*
	 * (non-Javadoc)
	 * @see org.javarosa.core.model.utils.IDataModelSerializingVisitor#serializeDataModel(org.javarosa.core.model.IFormDataModel)
	 */
	public byte[] serializeDataModel(IFormDataModel model) throws IOException {
		if(this.serializer == null) {
			this.setAnswerDataSerializer(new XFormAnswerDataSerializer());
		}
		model.accept(this);
		if(theXmlDoc != null) {
			return XFormSerializer.getString(theXmlDoc).getBytes("UTF-8");
		}
		else {
			return null;
		}
	}

	/*
	 * (non-Javadoc)
	 * @see org.javarosa.core.model.utils.IDataModelVisitor#visit(org.javarosa.core.model.IFormDataModel)
	 */
	public void visit(IFormDataModel dataModel) {
		if(dataModel.getClass() == DataModelTree.class) {
			this.visit((DataModelTree)dataModel);
		}
	}

	/*
	 * (non-Javadoc)
	 * @see org.javarosa.core.model.utils.IDataModelSerializingVisitor#setAnswerDataSerializer(org.javarosa.core.model.IAnswerDataSerializer)
	 */
	public void setAnswerDataSerializer(IAnswerDataSerializer ads) {
		this.serializer = ads;
	}

	public byte[] serializeDataModel(IFormDataModel model, FormDef formDef)
			throws IOException {
		this.schema = formDef;
		return serializeDataModel(model);
	}
}
