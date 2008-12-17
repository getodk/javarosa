package org.javarosa.model.xform;

import java.io.IOException;
import java.util.Enumeration;

import org.javarosa.core.model.FormDef;
import org.javarosa.core.model.IAnswerDataSerializer;
import org.javarosa.core.model.IFormDataModel;
import org.javarosa.core.model.instance.DataModelTree;
import org.javarosa.core.model.instance.TreeElement;
import org.javarosa.core.model.instance.TreeReference;
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
public class XFormSerializingVisitor implements IDataModelSerializingVisitor {

	/** The XML document containing the instance that is to be returned */
	Document theXmlDoc;

	/** The serializer to be used in constructing XML for AnswerData elements */
	IAnswerDataSerializer serializer;
	
	/** The schema to be used to serialize answer data */
	FormDef schema;	//not used

	public byte[] serializeDataModel(IFormDataModel model, FormDef formDef) throws IOException {
		this.schema = formDef;
		return serializeDataModel(model);
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
		if(dataModel instanceof DataModelTree) {
			this.visit((DataModelTree)dataModel);
		}
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.javarosa.core.model.utils.ITreeVisitor#visit(org.javarosa.core.model.DataModelTree)
	 */
	public void visit(DataModelTree tree) {
		theXmlDoc = new Document();
		TreeElement root = tree.getRoot();
		if (root != null)
			theXmlDoc.addChild(Node.ELEMENT, serializeNode(root));
	}

	public Element serializeNode (TreeElement instanceNode) {
		Element e = new Element(); //don't set anything on this element yet, as it might get overwritten

		//don't serialize template nodes or non-relevant nodes
		if (!instanceNode.isRelevant() || instanceNode.getMult() == TreeReference.INDEX_TEMPLATE)
			return null;
			
		if (instanceNode.getValue() != null) {
			Object serializedAnswer = serializer.serializeAnswerData(instanceNode.getValue(), instanceNode.dataType); 

			if (serializedAnswer instanceof Element) {
				e = (Element)serializedAnswer;
			} else if (serializedAnswer instanceof String) {
				e = new Element();
				e.addChild(Node.TEXT, (String)serializedAnswer);				
			} else {
				throw new RuntimeException("Can't handle serialized output");
			}

		} else {
			for (int i = 0; i < instanceNode.getNumChildren(); i++) {
				Element child = serializeNode((TreeElement)instanceNode.getChildren().elementAt(i));
				if (child != null)
					e.addChild(Node.ELEMENT, child);
			}
		}

		e.setName(instanceNode.getName());
		
		// add hard-coded attributes
		for (int i = 0; i < instanceNode.getAttributeCount(); i++) {
			String namespace = instanceNode.getAttributeNamespace(i);
			String name		 = instanceNode.getAttributeName(i);
			String val		 = instanceNode.getAttributeValue(i);
			e.setAttribute(namespace, name, val);
		}

		return e;
	}

	/*
	 * (non-Javadoc)
	 * @see org.javarosa.core.model.utils.IDataModelSerializingVisitor#setAnswerDataSerializer(org.javarosa.core.model.IAnswerDataSerializer)
	 */
	public void setAnswerDataSerializer(IAnswerDataSerializer ads) {
		this.serializer = ads;
	}
}
