package org.javarosa.dtree.i18n;

import org.javarosa.clforms.util.SimpleOrderedHashtable;

public class XFormsLocalizer implements ILocalizer {
	private SimpleOrderedHashtable dataMap;
	   
	public XFormsLocalizer(SimpleOrderedHashtable dataMap){
		this.dataMap = dataMap;
	}

	public String getText(String textId) {
            return (String) (dataMap.get(textId));
        }

    public String getText(String locale, String textId) {
        return (String) (dataMap.get(textId));
    }

    public SimpleOrderedHashtable getSelectMap(String selectMapId) {
        return (SimpleOrderedHashtable) (dataMap.get(selectMapId));
    }

    public SimpleOrderedHashtable getSelectMap(String selectMapId, String locale) {
        return (SimpleOrderedHashtable) (dataMap.get(selectMapId));
    }
}
