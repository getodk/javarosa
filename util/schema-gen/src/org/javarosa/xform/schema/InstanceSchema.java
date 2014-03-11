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

package org.javarosa.xform.schema;

import java.util.Hashtable;
import java.util.Vector;

import org.javarosa.core.model.Constants;
import org.javarosa.core.model.FormDef;
import org.javarosa.core.model.IFormElement;
import org.javarosa.core.model.ItemsetBinding;
import org.javarosa.core.model.QuestionDef;
import org.javarosa.core.model.SelectChoice;
import org.javarosa.core.model.instance.FormInstance;
import org.javarosa.core.model.instance.TreeElement;
import org.javarosa.core.model.instance.TreeReference;
import org.kxml2.kdom.Document;
import org.kxml2.kdom.Element;
import org.kxml2.kdom.Node;

public class InstanceSchema {
	private static Hashtable choiceTypeMapping;

	public static Document generateInstanceSchema (FormDef f) {
		init();

		Element schema = new Element();
		schema.setName("schema");
		schema.setNamespace("http://www.w3.org/2001/XMLSchema");
		schema.setPrefix("", "http://www.w3.org/2001/XMLSchema");
		schema.setPrefix("jr", "http://openrosa.org/javarosa");
		if (f.getInstance().schema != null) {
			schema.setAttribute(null, "targetNamespace", f.getInstance().schema);
		} else {
			System.err.println("Warning: instance has no schema");
		}
		schema.setAttribute(null, "elementFormDefault", "qualified");

		String formVersion = f.getInstance().formVersion;
		String uiVersion = f.getInstance().uiVersion;
		if (formVersion != null)
			schema.setAttribute(null, "version", formVersion);
		if (uiVersion != null)
			schema.setAttribute(null, "uiVersion", uiVersion);

		processSelectChoices(schema, f, f);
		schema.addChild(Node.ELEMENT, schemizeInstance(f.getInstance().getRoot()));

		Document schemaXML = new Document();
		schemaXML.addChild(Node.ELEMENT, schema);

		return schemaXML;
	}

	private static void init () {
		choiceTypeMapping = new Hashtable();
	}

	private static Element schemizeInstance (TreeElement node) {
		String name = node.getName();
		boolean terminal = node.isLeaf();
		boolean repeatable = node.isRepeatable();

		if (repeatable && node.getMult() != TreeReference.INDEX_TEMPLATE) {
			return null;
		}

		Element e = new Element();
		e.setName("element");
		e.setAttribute(null, "name", name);
		e.setAttribute(null, "minOccurs", "0"); //technically only needed if node has a 'relevant' attribute bound to it, but no easy way to tell
		if (repeatable) {
			e.setAttribute(null, "maxOccurs", "unbounded");
		}

		if (!terminal) {
			Element ct = new Element();
			ct.setName("complexType");
			e.addChild(Node.ELEMENT, ct);

			Element seq = new Element();
			seq.setName("sequence");
			ct.addChild(Node.ELEMENT, seq);

			for (int i = 0; i < node.getNumChildren(); i++) {
				Element child = schemizeInstance((TreeElement)node.getChildAt(i));
				if (child != null) {
					seq.addChild(Node.ELEMENT, child);
				}
			}
		} else {
			String type;

			switch (node.getDataType()) {
			case Constants.DATATYPE_NULL:
			case Constants.DATATYPE_TEXT:
				type = "string";
				break;
			case Constants.DATATYPE_INTEGER: type = "integer"; break;
			case Constants.DATATYPE_LONG: type = "long"; break;
			case Constants.DATATYPE_DECIMAL: type = "decimal"; break;
			case Constants.DATATYPE_BOOLEAN: type = "boolean"; break;
			case Constants.DATATYPE_DATE: type = "date"; break;
			case Constants.DATATYPE_DATE_TIME: type = "dateTime"; break;
			case Constants.DATATYPE_TIME: type = "time"; break;
			case Constants.DATATYPE_CHOICE:
			case Constants.DATATYPE_CHOICE_LIST:
				type = (String)choiceTypeMapping.get(node);
				if (type == null) {
					System.err.println("can't find choices for select-type question [" + node.getName() + "]");
				}
				break;
			case Constants.DATATYPE_GEOPOINT: type = "jr:geopoint"; break;
			case Constants.DATATYPE_GEOLINE: type = "jr:geoline"; break;
			case Constants.DATATYPE_GEOSHAPE: type = "jr:geoshape"; break;
			default:
				type = null;
				System.err.println("unrecognized type [" + node.getDataType() + ";" + node.getName() + "]");
				break;
			}

			if (type != null) {
				e.setAttribute(null, "type", type);
			}
		}

		return e;
	}

	private static void processSelectChoices (Element e, IFormElement fe, FormDef form) {
		if (fe instanceof QuestionDef) {
			QuestionDef q = (QuestionDef)fe;
			int controlType = q.getControlType();
			TreeReference ref = (TreeReference)q.getBind().getReference();

			if (controlType == Constants.CONTROL_SELECT_ONE || controlType == Constants.CONTROL_SELECT_MULTI) {
				String choiceTypeName = getChoiceTypeName(ref);

				Vector<SelectChoice> choices;
				//Figure out the choices involved if they are complex
				ItemsetBinding itemset = q.getDynamicChoices();
		    	if (itemset != null) {
		    		form.populateDynamicChoices(itemset, ref);
		    		choices = itemset.getChoices();
		    	} else { //static choices
		    		choices = q.getChoices();
		    	}


				writeChoices(e, choiceTypeName, choices);

				if (controlType == Constants.CONTROL_SELECT_MULTI) {
					writeListType(e, choiceTypeName);
				}

				choiceTypeMapping.put(form.getInstance().getTemplatePath(ref),
						(controlType == Constants.CONTROL_SELECT_MULTI ? "list." : "") + choiceTypeName);
			}
		} else {
			for (int i = 0; i < fe.getChildren().size(); i++) {
				processSelectChoices(e, fe.getChild(i), form);
			}
		}
	}

	private static String getChoiceTypeName (TreeReference ref) {
		return ref.toString(false).replace('/', '_');
	}

	private static void writeChoices (Element e, String typeName, Vector<SelectChoice> choices) {
		Element st = new Element();
		st.setName("simpleType");
		st.setAttribute(null, "name", typeName);
		e.addChild(Node.ELEMENT, st);

		Element restr = new Element();
		restr.setName("restriction");
		restr.setAttribute(null, "base", "string");
		st.addChild(Node.ELEMENT, restr);

		for (int i = 0; i < choices.size(); i++) {
			String value = choices.elementAt(i).getValue();

			Element choice = new Element();
			choice.setName("enumeration");
			choice.setAttribute(null, "value", value);
			restr.addChild(Node.ELEMENT, choice);
		}
	}

	private static void writeListType (Element e, String typeName) {
		Element st = new Element();
		st.setName("simpleType");
		st.setAttribute(null, "name", "list." + typeName);
		e.addChild(Node.ELEMENT, st);

		Element list = new Element();
		list.setName("list");
		list.setAttribute(null, "itemType", typeName);
		st.addChild(Node.ELEMENT, list);
	}
}
