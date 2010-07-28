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
import java.util.Enumeration;
import java.util.Vector;

import org.javarosa.core.data.IDataPointer;
import org.javarosa.core.model.FormDef;
import org.javarosa.core.model.IAnswerDataSerializer;
import org.javarosa.core.model.instance.FormInstance;
import org.javarosa.core.model.instance.TreeElement;
import org.javarosa.core.model.instance.TreeReference;
import org.javarosa.core.model.utils.IInstanceSerializingVisitor;
import org.javarosa.core.services.transport.payload.ByteArrayPayload;
import org.javarosa.core.services.transport.payload.DataPointerPayload;
import org.javarosa.core.services.transport.payload.IDataPayload;
import org.javarosa.core.services.transport.payload.MultiMessagePayload;
import org.javarosa.xform.util.XFormAnswerDataSerializer;
import org.kxml2.kdom.Element;
import org.kxml2.kdom.Node;

/**
 * A modified version of Clayton's XFormSerializingVisitor that constructs
 * SMS's.
 * 
 * @author Munaf Sheikh, Cell-Life
 * 
 */
public class SMSSerializingVisitor implements IInstanceSerializingVisitor {

	private String theSmsStr = null; // sms string to be returned
	private String smsNumber = null; // sms destination number
	private String nodeSet = null; // which nodeset the sms contents are in
	private String xmlns = null;
	private String delimeter = null;
	private String prefix = null;
	private String method = null;

	/** The serializer to be used in constructing XML for AnswerData elements */
	IAnswerDataSerializer serializer;

	/** The schema to be used to serialize answer data */
	FormDef schema; // not used

	Vector dataPointers;

	private void init() {
		theSmsStr = null;
		schema = null;
		dataPointers = new Vector();
	}

	public byte[] serializeInstance(FormInstance model, FormDef formDef)
			throws IOException {
		init();
		this.schema = formDef;
		return serializeInstance(model);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.javarosa.core.model.utils.IInstanceSerializingVisitor#serializeDataModel
	 * (org.javarosa.core.model.IFormDataModel)
	 */
	public byte[] serializeInstance(FormInstance model) throws IOException {
		init();
		if (this.serializer == null) {
			this.setAnswerDataSerializer(new XFormAnswerDataSerializer());
		}
		model.accept(this);
		if (theSmsStr != null) {
			return theSmsStr.getBytes("UTF-8");
		} else {
			return null;
		}
	}

	public IDataPayload createSerializedPayload(FormInstance model)
			throws IOException {
		init();
		if (this.serializer == null) {
			this.setAnswerDataSerializer(new XFormAnswerDataSerializer());
		}
		model.accept(this);
		if (theSmsStr != null) {
			byte[] form = theSmsStr.getBytes("UTF-8");
			if (dataPointers.size() == 0) {
				if (smsNumber.length() == 0) {
					return new ByteArrayPayload(form, null,
							IDataPayload.PAYLOAD_TYPE_SMS);
				}
				return new ByteArrayPayload(form, null,
						IDataPayload.PAYLOAD_TYPE_SMS, smsNumber);
			}
			MultiMessagePayload payload = new MultiMessagePayload();
			payload.addPayload(new ByteArrayPayload(form, null,
					IDataPayload.PAYLOAD_TYPE_SMS));
			Enumeration en = dataPointers.elements();
			while (en.hasMoreElements()) {
				IDataPointer pointer = (IDataPointer) en.nextElement();
				payload.addPayload(new DataPointerPayload(pointer));
			}
			return payload;
		} else {
			return null;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.javarosa.core.model.utils.ITreeVisitor#visit(org.javarosa.core.model
	 * .DataModelTree)
	 */
	public void visit(FormInstance tree) {
		smsNumber = new String();
		nodeSet = new String();

		TreeElement root = tree.getRoot();

		// get contents of submission blocks
		for (TreeElement te : root.getChildrenWithName("submission")) {
			method = te.getAttributeValue("", "method");
			smsNumber = te.getAttributeValue("", "action");
			nodeSet = te.getAttributeValue("", "nodeset");
		}

		// come in here only if there is a valid action and nodeset to parse
		if ((nodeSet != null) && (smsNumber != null)) {
			theSmsStr = "";
			String nset = nodeSet;

			XPathReference reference = new XPathReference(nset);
			TreeElement t = tree.resolveReference(reference);

			xmlns = t.getAttributeValue("", "xmlns");
			delimeter = t.getAttributeValue("", "delimeter");
			prefix = t.getAttributeValue("", "prefix");

			xmlns = (xmlns != null)? xmlns : " ";
			delimeter = (delimeter != null ) ? delimeter : " ";
			prefix = (prefix != null) ? prefix : " ";
			
			theSmsStr += prefix + delimeter;

			// serialize each node to get it's answers
			for (int j = 0; j < t.getNumChildren(); j++) {
				TreeElement tee = t.getChildAt(j);
				String e = serializeNode(tee);
				theSmsStr += e;
			}
			theSmsStr = theSmsStr.trim();

		} else {
			return;
		}

	}

	public String serializeNode(TreeElement instanceNode) {
		String ae = "";
		// don't serialize template nodes or non-relevant nodes
		if (!instanceNode.isRelevant()
				|| instanceNode.getMult() == TreeReference.INDEX_TEMPLATE)
			return null;

		if (instanceNode.getValue() != null) {
			Object serializedAnswer = serializer.serializeAnswerData(
					instanceNode.getValue(), instanceNode.dataType);

			if (serializedAnswer instanceof Element) {
				// DON"T handle this.
				throw new RuntimeException("Can't handle serialized output for"
						+ instanceNode.getValue().toString() + ", "
						+ serializedAnswer);
			} else if (serializedAnswer instanceof String) {
				Element e = new Element();
				e.addChild(Node.TEXT, (String) serializedAnswer);

				String tag = instanceNode.getAttributeValue("", "tag");
				ae += ((tag != null) ? tag + delimeter : delimeter); // tag
																		// might
																		// be
																		// null

				for (int k = 0; k < e.getChildCount(); k++) {
					ae += e.getChild(k).toString() + delimeter;
				}

			} else {
				throw new RuntimeException("Can't handle serialized output for"
						+ instanceNode.getValue().toString() + ", "
						+ serializedAnswer);
			}

			if (serializer.containsExternalData(instanceNode.getValue())
					.booleanValue()) {
				IDataPointer[] pointer = serializer
						.retrieveExternalDataPointer(instanceNode.getValue());
				for (int i = 0; i < pointer.length; ++i) {
					dataPointers.addElement(pointer[i]);
				}
			}
		}
		return ae;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seeorg.javarosa.core.model.utils.IInstanceSerializingVisitor#
	 * setAnswerDataSerializer(org.javarosa.core.model.IAnswerDataSerializer)
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
