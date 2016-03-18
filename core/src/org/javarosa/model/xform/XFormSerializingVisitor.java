package org.javarosa.model.xform;


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



	import java.io.IOException;
   import java.util.ArrayList;
import java.util.List;

import org.javarosa.core.data.IDataPointer;
import org.javarosa.core.model.FormDef;
import org.javarosa.core.model.IAnswerDataSerializer;
import org.javarosa.core.model.IDataReference;
import org.javarosa.core.model.instance.FormInstance;
import org.javarosa.core.model.instance.TreeElement;
import org.javarosa.core.model.instance.TreeReference;
import org.javarosa.core.model.utils.IInstanceSerializingVisitor;
import org.javarosa.core.services.transport.payload.ByteArrayPayload;
import org.javarosa.core.services.transport.payload.DataPointerPayload;
import org.javarosa.core.services.transport.payload.IDataPayload;
import org.javarosa.core.services.transport.payload.MultiMessagePayload;
import org.javarosa.xform.util.XFormAnswerDataSerializer;
import org.javarosa.xform.util.XFormSerializer;
import org.kxml2.kdom.Document;
import org.kxml2.kdom.Element;
import org.kxml2.kdom.Node;

	/**
	 * A visitor-esque class which walks a FormInstance and constructs an XML document
	 * containing its instance.
	 *
	 * The XML node elements are constructed in a depth-first manner, consistent with
	 * standard XML document parsing.
	 *
	 * @author Clayton Sims
	 *
	 */
	public class XFormSerializingVisitor implements IInstanceSerializingVisitor {

		/** The XML document containing the instance that is to be returned */
		Document theXmlDoc;

		/** The serializer to be used in constructing XML for AnswerData elements */
		IAnswerDataSerializer serializer;

		/** The root of the xml document which should be included in the serialization **/
		TreeReference rootRef;

		/** The schema to be used to serialize answer data */
		FormDef schema;	//not used

      List<IDataPointer> dataPointers;

		boolean respectRelevance = true;

		public XFormSerializingVisitor() {
			this(true);
		}
		public XFormSerializingVisitor(boolean respectRelevance) {
			this.respectRelevance = respectRelevance;
		}

		private void init() {
			theXmlDoc = null;
			schema = null;
			dataPointers = new ArrayList<IDataPointer>(0);
		}

		public byte[] serializeInstance(FormInstance model, FormDef formDef) throws IOException {

			//LEGACY: Should remove
			init();
			this.schema = formDef;
			return serializeInstance(model);
		}

		public byte[] serializeInstance(FormInstance model) throws IOException {
			return serializeInstance(model, new XPathReference("/"));
		}

		/*
		 * (non-Javadoc)
		 * @see org.javarosa.core.model.utils.IInstanceSerializingVisitor#serializeDataModel(org.javarosa.core.model.IFormDataModel)
		 */
		public byte[] serializeInstance(FormInstance model, IDataReference ref) throws IOException {
			init();
			rootRef = FormInstance.unpackReference(ref);
			if(this.serializer == null) {
				this.setAnswerDataSerializer(new XFormAnswerDataSerializer());
			}

			model.accept(this);
			if(theXmlDoc != null) {
				return XFormSerializer.getUtfBytes(theXmlDoc);
			}
			else {
				return null;
			}
		}

		public IDataPayload createSerializedPayload	(FormInstance model) throws IOException {
			return createSerializedPayload(model, new XPathReference("/"));
		}

		public IDataPayload createSerializedPayload	(FormInstance model, IDataReference ref) throws IOException {
			init();
			rootRef = FormInstance.unpackReference(ref);
			if(this.serializer == null) {
				this.setAnswerDataSerializer(new XFormAnswerDataSerializer());
			}
			model.accept(this);
			if(theXmlDoc != null) {
				//TODO: Did this strip necessary data?
				byte[] form = XFormSerializer.getUtfBytes(theXmlDoc);
				if(dataPointers.size() == 0) {
					return new ByteArrayPayload(form, null, IDataPayload.PAYLOAD_TYPE_XML);
				}
				MultiMessagePayload payload = new MultiMessagePayload();
				payload.addPayload(new ByteArrayPayload(form, "xml_submission_file", IDataPayload.PAYLOAD_TYPE_XML));
            for (IDataPointer pointer : dataPointers) {
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
		 * @see org.javarosa.core.model.utils.ITreeVisitor#visit(org.javarosa.core.model.DataModelTree)
		 */
		public void visit(FormInstance tree) {
			theXmlDoc = new Document();
			//TreeElement root = tree.getRoot();

			TreeElement root = tree.resolveReference(rootRef);

			//For some reason resolveReference won't ever return the root, so we'll
			//catch that case and just start at the root.
			if(root == null) {
				root = tree.getRoot();
			}

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
			if ((respectRelevance && !instanceNode.isRelevant()) || instanceNode.getMult() == TreeReference.INDEX_TEMPLATE) {
				return null;
			}

			if (instanceNode.getValue() != null) {
				Object serializedAnswer;
				try {
					serializedAnswer = serializer.serializeAnswerData(instanceNode.getValue(), instanceNode.getDataType());
				} catch (RuntimeException ex) {
					throw new RuntimeException("Unable to serialize " + instanceNode.getValue().toString() + ". Exception: " + ex.toString());
				}

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
						dataPointers.add(pointer[i]);
					}
				}
			} else {
				//make sure all children of the same tag name are written en bloc
				List<String> childNames = new ArrayList<String>(instanceNode.getNumChildren());
				for (int i = 0; i < instanceNode.getNumChildren(); i++) {
					String childName = instanceNode.getChildAt(i).getName();
					if (!childNames.contains(childName))
						childNames.add(childName);
				}

				for (int i = 0; i < childNames.size(); i++) {
					String childName = (String)childNames.get(i);
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
				// is it legal for getAttributeValue() to return null? playing it safe for now and assuming yes
				if (val == null) {
					val = "";
				}
				e.setAttribute(namespace, name, val);
			}
			if(instanceNode.getNamespace() != null) {
				e.setNamespace(instanceNode.getNamespace());
			}

			return e;
		}

		/*
		 * (non-Javadoc)
		 * @see org.javarosa.core.model.utils.IInstanceSerializingVisitor#setAnswerDataSerializer(org.javarosa.core.model.IAnswerDataSerializer)
		 */
		public void setAnswerDataSerializer(IAnswerDataSerializer ads) {
			this.serializer = ads;
		}

		public IInstanceSerializingVisitor newInstance() {
			XFormSerializingVisitor modelSerializer = new XFormSerializingVisitor();
			modelSerializer.setAnswerDataSerializer(this.serializer);
			return modelSerializer;
		}
	}
