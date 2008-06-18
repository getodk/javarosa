package org.javarosa.clforms.api;

import java.util.Enumeration;

import org.javarosa.clforms.storage.Model;
import org.javarosa.clforms.storage.ModelMetaData;
import org.javarosa.clforms.util.SimpleOrderedHashtable;
import org.javarosa.dtree.i18n.*;

/* droos: this method is a major PITA. why do we have to convert back to XML to store the form? can't we just serialize
 * our form objects directly?
 */

public class ExternaliseFormMethod {

	private Form form;
	private String namespace = "xf";
	private String formatting ="\n";

	public ExternaliseFormMethod() {
		super();
		// TODO Auto-generated constructor stub
	}
	
	public String externaliseForm(Form formObj) {
		this.form = formObj;
		String result = "";
		
		result += writeDocumentHeader()+formatting;
		
		result += writeModel();
		
		result += writeFormControls()+formatting;
		
		result += writeDocumentFooter()+formatting;
		
		return result;
	}

	private String writeFormControls() {
		String result = "";
		
		for(int i=0; i<form.getPrompts().size();i++){
			result += "<"+namespace+":";
			// form control type
			result += writeFormControlType(form.getPrompt(i));
			// attributes
			result += writeBindingFCAttributes(form.getPrompt(i));
			result += writeCommonFCAttributes(form.getPrompt(i));
			result +=">"+formatting;
			// common children label, hint
			result+= writeCommonElements(form.getPrompt(i));
			
			//if selects write Items
			if(form.getPrompt(i).getFormControlType()==Constants.SELECT || form.getPrompt(i).getFormControlType()==Constants.SELECT1 ){
				result += writeItem(form.getPrompt(i));
			}
			
			result += "</"+namespace+":"+writeFormControlType(form.getPrompt(i))+">"+formatting;
		}
		
		return result;
	}

	private String writeItem(Prompt prompt) {
		String result = "";
		
		for (int i = 0; i < prompt.getSelectIDMap().size(); i++) {
			String text = (String)prompt.getSelectIDMap().keyAt(i);
			boolean type = ((Boolean)prompt.getSelectIDMapTrans().elementAt(i)).booleanValue();
			String value = (String)prompt.getSelectIDMap().get(text);

			result += "<"+namespace+":item>"+formatting;
			result += "<label";
			if (type) {
				result += " ref=\"jr:itext('" + escapeStr(text, true) + "')\">";
			} else {
				result += ">" + escapeStr(text, false);
			}
			result += "</label><value>" + escapeStr(value, false) + "</value>";
			result += "</"+namespace+":item>"+formatting;
		}
		
		return result;
	}

	
	private String writeCommonFCAttributes(Prompt prompt) {
		String result = "";
		
		try {
			//required - in bind statement
			/*if(prompt.isRequired())
				result += "required=\"true()\" ";	*/	
			//relevant
			if(prompt.getRelevantString()!=null)
				result += attribute("relevant", prompt.getRelevantString()) + " ";
			
            // Set appearance attribute
            if(prompt.getAppearanceString()!=null)
            	result += attribute("appearance", prompt.getAppearanceString()) + " ";
            
            // To support type attribute
            if(prompt.getTypeString()!=null)
            	result += attribute("type", prompt.getTypeString()) + " ";
			
		} catch (RuntimeException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return result;
	}

	private String writeBindingFCAttributes(Prompt prompt) {
		String result = "";
		//bind
		if(prompt.getId() != null)
			if (prompt.getBindID() != null) {
				result += attribute("bind", prompt.getId()) + " ";
			} else {
				result += attribute("ref", prompt.getXpathBinding());
			}
		return result;
	}

	private String writeFormControlType(Prompt prompt) {
		String result = "";
		
		switch (prompt.getFormControlType()) {
		case Constants.INPUT:
			result += "input ";
			break;
		case Constants.SELECT1:
			result += "select1 ";
			break;
		case Constants.SELECT:
			result += "select ";
			break;
        case Constants.TRIGGER:
            result += "trigger ";
            break;                          
        case Constants.OUTPUT_GRAPH:
        case Constants.OUTPUT:
            result += "output ";
            break;     
		}
		
		return result;
	}

	private String writeCommonElements(Prompt prompt) {
		String result = "";

		if(prompt.getLongTextId() != null) {
			String longTextId = prompt.getLongTextId().substring(0, prompt.getLongTextId().indexOf(";long"));
			result += "<label ref=\"jr:itext('" + escapeStr(longTextId, true) + "')\"></label>" + formatting;
		} else {
			result+= element("label", prompt.getLongText()) + formatting;
		}     

		if(prompt.getHintTextId() != null) {
			result += "<hint ref=\"jr:itext('" + escapeStr(prompt.getHintTextId(), true) + "')\"></hint>" + formatting;
		} else if (prompt.getHint() != null){
			result+= element("hint",prompt.getHint())+formatting;
		}
	
        return result;
	}

	private String element(String label, String text) {
		return "<" + namespace + ":" + label + ">" + escapeStr(text, false) + "</" + namespace + ":" + label + ">";
	}
	
	private String attribute (String attr, String value) {
		return attr + "=\"" + (value == null ? "" : escapeStr(value, true)) + "\"";
	}
	
	private String writeModel() {
		String result = "";
		
		result += writeModelOpen()+formatting;
		
		if (form.getLocalizer() != null)
			result += writeItext()+formatting;
		
		result += writeInstance()+formatting;
		
		result += writeBinds();
		
		result += "</"+namespace+":model>"+formatting;
		
		return result;
	}

	private String writeBinds() {
		String result = "";
			
		Enumeration enumeration = form.getBindings().elements();
		while (enumeration.hasMoreElements()) {
			Binding bind = (Binding) enumeration.nextElement();
			
			result += "<"+namespace+":bind ";
			
			result += writeBindAttributes(bind);
			
			result += "/>"+formatting;
		}

		return result;
	}

	private String writeBindAttributes(Binding bind) {
		String result = "";
		//id
		if(bind.getId()!=null)
			result += attribute("id", bind.getId()) + " ";
		//nodeset
		if(bind.getNodeset()!=null)
			result += attribute("nodeset", bind.getNodeset()) + " ";
		//required
		if(bind.isRequired())
			result += "required=\"true()\" ";
		//type
		if(bind.getType()!=null)
			result += attribute("type", "xsd:" + bind.getType()) + " ";
		//relevant
		if(bind.getRelevancy()!=null)
			result += attribute("relevant", bind.getRelevancy()) + " ";

		if (bind.preload != null) {
			result += attribute("jr:preload", bind.preload) + " ";
			if (bind.preloadParams != null) {
				result += attribute("jr:preloadParams", bind.preloadParams) + " ";
			}
		}
		
		return result;
	}

	private String writeInstance() {
		String result = "";
		
		result += "<"+namespace+":instance>"+formatting;
		
		result += writeModelData()+formatting;
		
		result += "</"+namespace+":instance>";
		
		return result;
	}

	private String writeModelData() {
		String result = "";
		
		result += form.getXmlModel().toString();
		
		return result;
	}

	private String writeModelOpen() {
		String result = "";
		Model model = form.getXmlModel();
		result += "<"+namespace+":model ";
		
		result += writeModelAttributes(model);
				
		result += ">";
		
		return result;
	}

	private String writeModelAttributes(Model model) {
		String result = "";
		ModelMetaData mdata = new ModelMetaData(model);
		System.out.println("EXTERNALIZE model: "+mdata.toString());
		
		result += attribute("id", mdata.getName());
				
		return result;
	}

	private String writeDocumentHeader() {
		return "" +
				"<html xmlns=\"http://www.w3.org/1999/xhtml\" xmlns:xf=\"http://www.w3.org/2002/xforms\" xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\" xmlns:jr=\"http://openrosa.org/javarosa\">";
	}

	private String writeDocumentFooter() {
		return "</html>";		
	}
	
    private String writeItext() {
         String result = "";
         Localizer l = form.getLocalizer();
         
         result += "<itext>"+formatting;
         
         String[] locales = l.getAvailableLocales();
         for (int i = 0; i < locales.length; i++) {
        	 String locale = locales[i];
        	 SimpleOrderedHashtable localeData = l.getLocaleData(locale);
        	 
        	 result += "<translation lang=\"" + escapeStr(locales[i], true) + "\"";
        	 if (locale.equals(l.getDefaultLocale()))
        		 result += " default=\"\"";
        	 result += ">"+formatting;
        	 
        	 for (Enumeration e = localeData.keys(); e.hasMoreElements(); ) {
        		 String textHandle = (String)e.nextElement();
        		 String text = (String)localeData.get(textHandle);
        		 String form = null;
        		 if (textHandle.indexOf(";") != -1) {
        			 form = textHandle.substring(textHandle.indexOf(";") + 1);
        			 textHandle = textHandle.substring(0, textHandle.indexOf(";"));
        		 }
        		 
        		 result += "<text id=\"" + escapeStr(textHandle, true) + "\"><value";
        		 if (form != null)
        			 result += " form=\"" + escapeStr(form, true) + "\"";
        		 result += ">" + escapeStr(text, false) + "</value></text>" + formatting;
        	 }
        	 
        	 result += "</translation>" + formatting;
         }
                  
         result += "</itext>";
         
         return result;
     }
	
	public static String escapeStr (String s, boolean escapeQuotes) {
		StringBuffer strBuff = new StringBuffer(s);
		
		replace(strBuff, "&", "&amp;");
		replace(strBuff, "<", "&lt;");
		replace(strBuff, ">", "&gt;");
		if (escapeQuotes) {
			replace(strBuff, "\"", "&quot;");
			replace(strBuff, "'", "&apos;");		
		}
		
		return strBuff.toString();
	}
	
	/* replace all instances of a string in a stringbuffer with another string
	 * no argument can be null; if findStr is empty string, no replacements are made
	 * after a replacement is made, searching resumes immediately after the replaced string (no overlapping)
	 */
	public static void replace (StringBuffer sb, String findStr, String replStr) {
		if (findStr.length() == 0)
			return;
		
		for (int i = 0; i <= sb.length() - findStr.length(); i++) {
			boolean match = true;
			for (int j = 0; j < findStr.length(); j++) {
				if (sb.charAt(i + j) != findStr.charAt(j)) {
					match = false;
					break;
				}
			}
			
			if (match) {
				for (int k = 0; k < Math.min(findStr.length(), replStr.length()); k++)
					sb.setCharAt(i + k, replStr.charAt(k));
				if (findStr.length() < replStr.length()) {
					sb.insert(i + findStr.length(), replStr.substring(findStr.length()));
				} else {
					sb.delete(i + replStr.length(), i + findStr.length());
				}
				
				i += replStr.length() - 1;
			}
		}
	}
}
