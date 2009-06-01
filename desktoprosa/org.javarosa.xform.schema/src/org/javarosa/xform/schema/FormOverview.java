package org.javarosa.xform.schema;

import java.util.Enumeration;

import org.javarosa.core.model.Constants;
import org.javarosa.core.model.FormDef;
import org.javarosa.core.model.GroupDef;
import org.javarosa.core.model.IDataReference;
import org.javarosa.core.model.IFormElement;
import org.javarosa.core.model.QuestionDef;
import org.javarosa.core.model.condition.Condition;
import org.javarosa.core.model.condition.IConditionExpr;
import org.javarosa.core.model.condition.Triggerable;
import org.javarosa.core.model.instance.DataModelTree;
import org.javarosa.core.model.instance.TreeElement;
import org.javarosa.core.model.instance.TreeReference;
import org.javarosa.core.services.locale.Localizer;
import org.javarosa.core.util.externalizable.ExtUtil;
import org.javarosa.model.xform.XPathReference;
import org.javarosa.xform.util.XFormAnswerDataSerializer;
import org.javarosa.xpath.XPathConditional;

public class FormOverview {
	public static String overview (FormDef f) {
		StringBuffer sb = new StringBuffer();
				
		println(sb, 0, "Form Name: " + f.getName());
		println(sb, 0, "Form Title: " + f.getTitle());
		println(sb);
		
		if (f.getLocalizer() != null) {
			Localizer l = f.getLocalizer();
			String[] langs = l.getAvailableLocales();
			
			println(sb, 0, "Available Languages: " + langs.length);
			for (int i = 0; i < langs.length; i++) {
				println(sb, 1, langs[i] + (langs[i].equals(l.getDefaultLocale()) ? " (default)" : ""));
			}
			
			f.getLocalizer().setToDefault();
		} else {
			println(sb, 0, "Available Languages: 1 (no multi-lingual content)");
		}
		println(sb);
		
		listQuestions(f, f, 0, sb);
		
		return sb.toString();
	}
	
	private static void listQuestions (FormDef f, IFormElement fe, int indent, StringBuffer sb) {
		if (fe instanceof QuestionDef) {
			listQuestion(f, (QuestionDef)fe, indent, sb);
		} else {
			if (fe instanceof GroupDef) {
				if (listGroup(f, (GroupDef)fe, indent, sb)) {
					indent += 1;
				}
			}
			
			for (int i = 0; i < fe.getChildren().size(); i++) {
				listQuestions(f, fe.getChild(i), indent, sb);
			}
		}
	}
	
	private static void listQuestion (FormDef f, QuestionDef q, int indent, StringBuffer sb) {
		TreeElement instanceNode = getInstanceNode(f.getDataModel(), q.getBind());
		
		String caption = q.getLongText();
		int type = instanceNode.dataType;
		
		if (q.getControlType() != Constants.CONTROL_TRIGGER) {
			println(sb, indent, "Question: \"" + caption + "\"");
			println(sb, indent + 1, "Type: " + printType(type));
		} else {
			println(sb, indent, "Info: \"" + caption + "\"");
		}
			
		if (q.getControlType() == Constants.CONTROL_SELECT_ONE || q.getControlType() == Constants.CONTROL_SELECT_MULTI) {
			printChoices(q, indent + 1, sb);
		}
		
		printProperty("relevant", f, instanceNode, indent + 1, sb);
		
		printProperty("required", f, instanceNode, indent + 1, sb);

		printProperty("readonly", f, instanceNode, indent + 1, sb);

		String defaultValue = printDefault(instanceNode);
		if (defaultValue != null) {
			println(sb, indent + 1, "Default: " + defaultValue);
		}
		
		if (instanceNode.getConstraint() != null) {
			println(sb, indent + 1, "Constraint: " + printCondition(instanceNode.getConstraint().constraint));
		}
		
		println(sb);
	}
	
	private static void printChoices (QuestionDef q, int indent, StringBuffer sb) {
		println(sb, indent, "Choices: " + q.getSelectItems().size());
		for (Enumeration e = q.getSelectItems().keys(); e.hasMoreElements(); ) {
			println(sb, indent + 1, "\"" + (String)e.nextElement() + "\"");
		}
	}
	
	private static void printProperty (String property, FormDef f, TreeElement instanceNode, int indent, StringBuffer sb) {
		String line = printConditionalProperty(property, f, instanceNode);
		if (line != null) {
			println(sb, indent, line);
		}
	}
	
	private static String printConditionalProperty (String property, FormDef f, TreeElement instanceNode) {
		int action = -1;
		String conditionHeader = null;
		boolean absolute = false;
		boolean absoluteReportable = false;
		String absoluteHeader = null;
		
		if (property.equals("relevant")) {
			action = Condition.ACTION_SHOW;
			conditionHeader = "Relevant if";
			absolute = instanceNode.relevant;
			absoluteReportable = false;
			absoluteHeader = "Never relevant";
		} else if (property.equals("required")) {
			action = Condition.ACTION_REQUIRE;
			conditionHeader = "Conditionally Required";
			absolute = instanceNode.required;
			absoluteReportable = true;
			absoluteHeader = "Required";
		} else if (property.equals("readonly")) {
			action = Condition.ACTION_DISABLE;
			conditionHeader = "Conditionally Read-only";
			absolute = instanceNode.isEnabled();
			absoluteReportable = false;
			absoluteHeader = "Read-only";
		}
		
		IConditionExpr expr = null;
		
		for (int i = 0; i < f.triggerables.size() && expr == null; i++) {
			// Clayton Sims - Jun 1, 2009 : Not sure how legitimate this 
			// cast is. It might work now, but break later.
			Condition c = (Condition)f.triggerables.elementAt(i);
						
			if (c.trueAction == action) {
				for (int j = 0; j < c.targets.size() && expr == null; j++) {
					TreeReference target = (TreeReference)c.targets.elementAt(j);
					
					if (instanceNode == getInstanceNode(f.getDataModel(), new XPathReference(target))) {
						expr = c.expr;
					}
				}
			}
		}
		
		String line = null;
		if (expr != null) {
			line = conditionHeader + ": " + printCondition(expr);
		} else if (absolute == absoluteReportable) {
			line = absoluteHeader;
		}
		
		return line;
	}
	
	private static String printDefault (TreeElement node) {
		String value = null;
		
		if (node.getPreloadHandler() != null) {
			if (node.getPreloadHandler().equals("date")) {
				if (node.getPreloadParams().equals("today")) {
					value = "Today's Date";
				}
			} else if (node.getPreloadHandler().equals("property")) {
				if (node.getPreloadParams().equals("DeviceID")) {
					value = "Device ID";
				}
			} else if (node.getPreloadHandler().equals("timestamp")) {
				if (node.getPreloadParams().equals("start")) {
					value = "Timestamp when form opened";
				} else if (node.getPreloadParams().equals("end")) {
					value = "Timestamp when form completed";
				}
			} else if (node.getPreloadHandler().equals("context")) {
				if (node.getPreloadParams().equals("UserID")) {
					value = "User ID";
				} else if (node.getPreloadParams().equals("UserName")) {
					value = "User Name";
				}	
			} else if (node.getPreloadHandler().equals("patient")) {
				value = "Patient Record: " + node.getPreloadParams();
			}
			
			if (value == null) {
				value = "Preload Handler: " + node.getPreloadHandler();
				if (node.getPreloadParams() != null) {
					value = value + "; params: " + node.getPreloadParams();
				}
			}
		} else {
			if (node.getValue() != null) {
				XFormAnswerDataSerializer xfads = new XFormAnswerDataSerializer();
				if (xfads.canSerialize(node.getValue())) {
					value = (String)xfads.serializeAnswerData(node.getValue(), node.dataType);
				} else {
					value = "unknown data";
				}
			}
		}
		
		return value;
	}
	
	private static String printCondition (IConditionExpr c) {
		String expr = ((XPathConditional)c).xpath;
		
		return (expr != null ? expr : "condition unavailable");
	}
	
	private static boolean listGroup (FormDef f, GroupDef g, int indent, StringBuffer sb) {
		boolean repeat = g.getRepeat();
		String caption = ExtUtil.nullIfEmpty(g.getLongText());
		TreeElement instanceNode = getInstanceNode(f.getDataModel(), g.getBind());
		
		String relevant = printConditionalProperty("relevant", f, instanceNode);
		String readonly = printConditionalProperty("readonly", f, instanceNode);
		
		if (repeat || caption != null || (relevant != null || readonly != null)) {
			println(sb, indent, (repeat ? "Repeat" : "Group") + ":" + (caption != null ? " \"" + caption + "\"" : ""));
			
			if (relevant != null) {
				println(sb, indent + 1, relevant);
			}
			
			if (readonly != null) {
				println(sb, indent + 1, readonly);
			}
				
			println(sb);
			
			return true;
		} else {
			return false;
		}
	}
	
	private static String printType (int type) {
		switch (type) {
		case Constants.DATATYPE_NULL:
		case Constants.DATATYPE_TEXT:
			return "text";
		case Constants.DATATYPE_INTEGER: return "integer";
		case Constants.DATATYPE_DECIMAL: return "decimal";
		case Constants.DATATYPE_DATE: return "date";
		case Constants.DATATYPE_DATE_TIME: return "date with time";
		case Constants.DATATYPE_TIME: return "time of day";
		case Constants.DATATYPE_CHOICE: return "single select";
		case Constants.DATATYPE_CHOICE_LIST: return "multi select";
		default: return "unrecognized type [" + type + "]";
		}
	}
	
	private static TreeElement getInstanceNode (DataModelTree d, IDataReference ref) {
		return d.getTemplatePath((TreeReference)ref.getReference());
	}
	
	private static void println (StringBuffer sb, int indent, String line) {
		for (int i = 0; i < indent; i++) {
			sb.append("  ");
		}
		sb.append(line + "\n");
	}
	
	private static void println (StringBuffer sb) {
		println(sb, 0, "");
	}
}
