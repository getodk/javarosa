package org.javarosa.core.model.test.utils;

import org.javarosa.core.model.FormDef;
import org.javarosa.core.model.GroupDef;
import org.javarosa.core.model.QuestionDef;

/**
 * Static methods to create form defs for workflow testing.
 * 
 * @author Clayton Sims
 *
 */
public class FormDefConstructionUtils {
	
	public static FormDef createSimpleGroupReference() {
		FormDef theform = new FormDef();
		
		QuestionDef question1 = new QuestionDef();
		GroupDef group1 = new GroupDef();
		QuestionDef question11 = new QuestionDef();
		QuestionDef question12 = new QuestionDef();
		group1.addChild(question11);
		group1.addChild(question12);
		QuestionDef question2 = new QuestionDef();
		theform.addChild(question1);
		theform.addChild(group1);
		theform.addChild(question2);
		
		return theform;
	}
}
