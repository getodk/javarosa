package org.javarosa.formmanager.utility;

import org.javarosa.form.api.FormEntryPrompt;

public class WidgetUtil {
	
	
	/**
	 * Use this, usually in refresh/updateWidget() method to do
	 * the text form fallback boogey.
	 * 
	 * Fallback logic:
	 * Try get the "long" form,
	 * then try get the "short" form,
	 * then try get the default form.
	 * 
	 * If through all of this textID is actually null,
	 * this method will return the LabelInnerText.
	 * 
	 * @param textID
	 * @returns text of the appropriate form.
	 */
	public static String getAppropriateTextForm(FormEntryPrompt fep,String textID){
		String caption;
		
		if(fep.getAvailableTextFormTypes(textID).contains("long")){
			caption = fep.getLongText();
		}else if(fep.getAvailableTextFormTypes(textID).contains("short")){
			caption = fep.getShortText();
		}else{
			caption = fep.getDefaultText();
		}
		return caption;
	}

}
