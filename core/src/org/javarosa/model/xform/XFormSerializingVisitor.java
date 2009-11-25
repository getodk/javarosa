/*
 * Copyright (C) 2009 JavaRosa
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package org.javarosa.model.xform;

import java.io.IOException;
import java.util.Enumeration;
import java.util.Vector;

import org.javarosa.core.data.IDataPointer;
import org.javarosa.core.model.FormDef;
import org.javarosa.core.model.IAnswerDataSerializer;
import org.javarosa.core.model.IFormDataModel;
import org.javarosa.core.model.instance.DataModelTree;
import org.javarosa.core.model.instance.TreeElement;
import org.javarosa.core.model.instance.TreeReference;
import org.javarosa.core.model.utils.IDataModelSerializingVisitor;
import org.javarosa.core.services.transport.ByteArrayPayload;
import org.javarosa.core.services.transport.DataPointerPayload;
import org.javarosa.core.services.transport.IDataPayload;
import org.javarosa.core.services.transport.MultiMessagePayload;
import org.javarosa.xform.util.XFormAnswerDataSerializer;
import org.javarosa.xform.util.XFormSerializer;
import org.kxml2.kdom.Document;
import org.kxml2.kdom.Element;
import org.kxml2.kdom.Node;

/**
 * A visitor-esque class which walks a DataModelTree and constructs an XML document
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
	
	Vector dataPointers;
	
	private void init() {
		theXmlDoc = null;
		schema = null;
		dataPointers = new Vector();
	}

	public byte[] serializeDataModel(IFormDataModel model, FormDef formDef) throws IOException {
		init();
		this.schema = formDef;
		return serializeDataModel(model);
	}

	/*
	 * (non-Javadoc)
	 * @see org.javarosa.core.model.utils.IDataModelSerializingVisitor#serializeDataModel(org.javarosa.core.model.IFormDataModel)
	 */
	public byte[] serializeDataModel(IFormDataModel model) throws IOException {
		init();
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
	
	public IDataPayload createSerializedPayload	(IFormDataModel model) throws IOException {
		init();
		if(this.serializer == null) {
			this.setAnswerDataSerializer(new XFormAnswerDataSerializer());
		}
		model.accept(this);
		if(theXmlDoc != null) {
			byte[] form = XFormSerializer.getString(theXmlDoc).getBytes("UTF-8");
			if(dataPointers.size() == 0) {
				return new ByteArrayPayload(form, null, IDataPayload.PAYLOAD_TYPE_XML);
			}
			MultiMessagePayload payload = new MultiMessagePayload();
			payload.addPayload(new ByteArrayPayload(form, null, IDataPayload.PAYLOAD_TYPE_XML));
			Enumeration en = dataPointers.elements();
			while(en.hasMoreElements()) {
				IDataPointer pointer = (IDataPointer)en.nextElement();
				payload.addPayload(new DataPointerPayload(pointer));
			}
			return payload; 
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
		if (root != null) {
			theXmlDoc.addChild(Node.ELEMENT, serializeNode(root));
		}
		
		Element top = theXmlDoc.getElement(0);
		
		String[] prefixes = tree.getNamespacePrefixes();
		for(int i = 0 ; i < prefixes.length; ++i ) {
			top.setPrefix(prefixes[i], tree.getNamespaceURI(prefixes[i]));
		}
		if (tree.schema != null) {
			top.setNamespace(tree.schema);
			top.setPrefix("", tree.schema);
		}
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
				throw new RuntimeException("Can't handle serialized output for" + instanceNode.getValue().toString() + ", " + serializedAnswer);
			}
			
			if(serializer.containsExternalData(instanceNode.getValue()).booleanValue()) {
				IDataPointer[] pointer = serializer.retrieveExternalDataPointer(instanceNode.getValue());
				for(int i = 0 ; i < pointer.length ; ++i) {
					dataPointers.addElement(pointer[i]);
				}
			}
		} else {
			//make sure all children of the same tag name are written en bloc
			Vector childNames = new Vector();
			for (int i = 0; i < instanceNode.getNumChildren(); i++) {
				String childName = ((TreeElement)instanceNode.getChildren().elementAt(i)).getName();
				if (!childNames.contains(childName))
					childNames.addElement(childName);
			}
			
			for (int i = 0; i < childNames.size(); i++) {
				String childName = (String)childNames.elementAt(i);
				int mult = instanceNode.getChildMultiplicity(childName);
				for (int j = 0; j < mult; j++) {
					Element child = serializeNode(instanceNode.getChild(childName, j));
					if (child != null) {
						e.addChild(Node.ELEMENT, child);
					}
				}
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
	
	public IDataModelSerializingVisitor newInstance() {
		XFormSerializingVisitor modelSerializer = new XFormSerializingVisitor();
		modelSerializer.setAnswerDataSerializer(this.serializer);
		return modelSerializer;
	}
}
