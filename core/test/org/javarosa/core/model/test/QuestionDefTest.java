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

package org.javarosa.core.model.test;

import j2meunit.framework.Test;
import j2meunit.framework.TestCase;
import j2meunit.framework.TestMethod;
import j2meunit.framework.TestSuite;

import java.util.Vector;

import org.javarosa.core.model.Constants;
import org.javarosa.core.model.FormElementStateListener;
import org.javarosa.core.model.FormIndex;
import org.javarosa.core.model.IDataReference;
import org.javarosa.core.model.IFormElement;
import org.javarosa.core.model.QuestionDef;
import org.javarosa.core.model.SelectChoice;
import org.javarosa.core.model.instance.TreeElement;
import org.javarosa.core.reference.InvalidReferenceException;
import org.javarosa.core.reference.Reference;
import org.javarosa.core.reference.ReferenceManager;
import org.javarosa.core.reference.ResourceReferenceFactory;
import org.javarosa.core.reference.RootTranslator;
import org.javarosa.core.services.PrototypeManager;
import org.javarosa.core.services.locale.Localizer;
import org.javarosa.core.services.locale.TableLocaleSource;
import org.javarosa.core.test.FormParseInit;
import org.javarosa.core.util.externalizable.ExtUtil;
import org.javarosa.core.util.externalizable.PrototypeFactory;
import org.javarosa.form.api.FormEntryCaption;
import org.javarosa.form.api.FormEntryController;
import org.javarosa.form.api.FormEntryPrompt;

public class QuestionDefTest extends TestCase {
	QuestionDef q = null;
	FormEntryPrompt fep = null;
	FormParseInit fpi = null;
	
	public QuestionDefTest(String name, TestMethod rTestMethod) {
		super(name, rTestMethod);
		initStuff();
	}
	
	public QuestionDefTest(String name) {
		super(name);
		initStuff();
	}
	
	public QuestionDefTest() {
		super();
		initStuff();
	}	
	
	public void initStuff(){
		fpi = new FormParseInit();
		q = fpi.getFirstQuestionDef();
		fep = new FormEntryPrompt(fpi.getFormDef(), fpi.getFormEntryModel().getFormIndex());
	}
	
	static PrototypeFactory pf;
	
	static {
		PrototypeManager.registerPrototype("org.javarosa.model.xform.XPathReference");
		pf = ExtUtil.defaultPrototypes();
	}
		
	public Test suite() {
		TestSuite aSuite = new TestSuite();
		
		for (int i = 1; i <= NUM_TESTS; i++) {
			final int testID = i;
			aSuite.addTest(new QuestionDefTest("QuestionDef Test " + i, new TestMethod() {
				public void run (TestCase tc) {
					((QuestionDefTest)tc).doTest(testID);
				}
			}));
		}
			
		return aSuite;
	}
	
	private void testSerialize (QuestionDef q, String msg) {
		//ExternalizableTest.testExternalizable(q, this, pf, "QuestionDef [" + msg + "]");
	}
	
	public final static int NUM_TESTS = 12;
	public void doTest (int i) {
		switch (i) {
		case 1: testConstructors(); break;
		case 2: testAccessorsModifiers(); break;
		case 3: testChild(); break;
		case 4: testFlagObservers(); break;
		case 5: testPromptsNoLocalizer(); break;
		case 6: testPromptIDsNoLocalizer(); break;
		case 7: testPromptsWithLocalizer(q,fep); break;
		case 8: testSelectChoicesNoLocalizer(); break;
		case 9: testSelectChoiceIDsNoLocalizer(); break;
		case 10: testNonLocalizedText(); break;
		case 11: testTextForms(); break;
		case 12: testReferences(); break;
		}
	}
	
	public void testConstructors () {
		QuestionDef q;
		
		q = new QuestionDef();
		if (q.getID() != -1) {
			fail("QuestionDef not initialized properly (default constructor)");
		}
		testSerialize(q, "a");
		
		q = new QuestionDef(17,Constants.CONTROL_RANGE);
		if (q.getID() != 17) {
			fail("QuestionDef not initialized properly");
		}
		testSerialize(q, "b");
	}

	public IDataReference newRef (String xpath) {
			IDataReference ref = new DummyReference();
			ref.setReference(xpath);
			pf.addClass(DummyReference.class);
			return ref;
	}
	
	public void testAccessorsModifiers () {
		QuestionDef q = new QuestionDef();
		
		q.setID(45);
		if (q.getID() != 45) {
			fail("ID getter/setter broken");
		}
		testSerialize(q, "c");

		IDataReference ref = newRef("/data");
		q.setBind(ref);
		if (q.getBind() != ref) {
			fail("Ref getter/setter broken");
		}
		testSerialize(q, "e");

		q.setControlType(Constants.CONTROL_SELECT_ONE);
		if (q.getControlType() != Constants.CONTROL_SELECT_ONE) {
			fail("Control type getter/setter broken");
		}
		testSerialize(q, "g");

		q.setAppearanceAttr("minimal");
		if (!"minimal".equals(q.getAppearanceAttr())) {
			fail("Appearance getter/setter broken");
		}
		testSerialize(q, "h");
	}
		
	public void testChild () {
		QuestionDef q = new QuestionDef();
		
		if (q.getChildren() != null) {
			fail("Question has children");
		}

		try {
			q.setChildren(new Vector());
			fail("Set a question's children without exception");
		} catch (IllegalStateException ise) {
			//expected
		}
		
		try {
			q.addChild(new QuestionDef());
			fail("Added a child to a question without exception");
		} catch (IllegalStateException ise) {
			//expected
		}
	}
	
	public void testFlagObservers () {
		QuestionDef q = new QuestionDef();

		QuestionObserver qo = new QuestionObserver();
		q.registerStateObserver(qo);

		if (qo.flag || qo.q != null || qo.flags != 0) {
			fail("Improper state in question observer");
		}

		q.unregisterStateObserver(qo);
		
		if (qo.flag) {
			fail("Localization observer updated after unregistered");
		}
	}

	public void testPromptsNoLocalizer () {
		QuestionDef q = new QuestionDef();
		
		q.setLabelInnerText("labelInnerText");
		if (!"labelInnerText".equals(q.getLabelInnerText())) {
			fail("LabelInnerText getter/setter broken");
		}

		q.setHelpText("help text");
		if (!"help text".equals(q.getHelpText())) {
			fail("Help text getter/setter broken");
		}
	}
	
	public void testPromptIDsNoLocalizer () {
		QuestionDef q = new QuestionDef();
		
		q.setTextID("long text id");
		if (!"long text id".equals(q.getTextID())) {
			fail("Long text ID getter/setter broken");
		}

		q.setHelpTextID("help text id");
		if (!"help text id".equals(q.getHelpTextID()) || q.getHelpText() != null) {
			fail("Help text ID getter/setter broken");
		}
	}
	
	public void testPromptsWithLocalizer (QuestionDef q, FormEntryPrompt fep) {
		if(q==null||fep==null) fail("QuestionDef and Localizer not set in QuestionDefTest!");
		
		Localizer l = fep.getLocalizer();

		TableLocaleSource table = new TableLocaleSource();
		l.addAvailableLocale("locale");
		l.setDefaultLocale("locale");
		table.setLocaleMapping("prompt;long", "loc: long text");
		table.setLocaleMapping("prompt;short", "loc: short text");
		table.setLocaleMapping("help", "loc: help text");
		l.registerLocaleResource("locale", table);
		
		l.setLocale("locale");
		
		q.setTextID("prompt");

		if (!"loc: long text".equals(fep.getLongText(q.getTextID()))) {
			fail("Long text did not localize properly");
		}
		if (!"loc: short text".equals(fep.getShortText(q.getTextID()))){
			fail("Short text did not localize properly");
		}
		
		testSerialize(q, "t");
	
		q.setHelpTextID("help");
		if (!"loc: help text".equals(fep.getHelpText())) {
			fail("Help text did not localize when setting ID");
		}
		testSerialize(q, "v");
	}

	public void testSelectChoicesNoLocalizer () {		
		QuestionDef q = fpi.getFirstQuestionDef();
		if (q.getNumChoices() != 0) {
			fail("Select choices not empty on init");
		}

		q.addSelectChoice(new SelectChoice("","choice", "val", false));
		q.addSelectChoice(new SelectChoice("","stacey's", "mom", false));
		
		
		if(!fep.getSelectChoices().toString().equals("[choice => val, stacey's => mom]")) {
//		if (!q.getChoices().toString().equals("[choice => val, stacey's => mom]")) {
			fail("Could not add individual select choice"+fep.getSelectChoices().toString());
		}
		testSerialize(q, "w");
		
		q.removeSelectChoice(q.getChoice(0));
		q.removeSelectChoice(q.getChoice(0));
	}
	
	public void testSelectChoiceIDsNoLocalizer () {
		
		QuestionDef q = fpi.getFirstQuestionDef();
		
		q.addSelectChoice(new SelectChoice("choice1 id", "val1"));
		q.addSelectChoice(new SelectChoice("loc: choice2", "val2", false));
		
		if (!fep.getSelectChoices().toString().equals("[{choice1 id} => val1, loc: choice2 => val2]")) {
			fail("Could not add individual select choice ID"+fep.getSelectChoices().toString());
		}
		testSerialize(q, "y");
		
		//clean up
		q.removeSelectChoice(q.getChoices().elementAt(0));
		q.removeSelectChoice(q.getChoices().elementAt(0));
	}
	
	public void testTextForms(){
		FormEntryController fec = fpi.getFormEntryController();
		fec.jumpToIndex(FormIndex.createBeginningOfFormIndex());
		boolean foundFlag = false;
		Localizer l = fpi.getFormDef().getLocalizer();
		
		l.setDefaultLocale(l.getAvailableLocales()[0]);
		l.setLocale(l.getAvailableLocales()[0]);
		
		//test image long text
		do{
			if(!(fpi.getFormEntryModel().getCaptionPrompt().getFormElement() instanceof QuestionDef)) continue;
			
			fep = fpi.getFormEntryModel().getQuestionPrompt();
			q = fpi.getCurrentQuestion();
			if(q.getTextID() == null) continue;
			//yes. It's a little ugly. -Anton
			if(q.getTextID().equals("name")){
				if(fep.getAvailableTextFormTypes(q.getTextID()).contains("image")){
					if(!"jr://images/four.gif".equals(fep.getImageText())){
						fail ("getImageText is being faulty.");
					}else{
						foundFlag = true;
					}
				}
			}	
		}while(fec.stepToNextEvent()!=FormEntryController.EVENT_END_OF_FORM);
		if(!foundFlag) fail("Couldn't find QuestionDef with TextID [name] with available image/audio text form.");
		foundFlag = false;
		
		fec.jumpToIndex(FormIndex.createBeginningOfFormIndex()); //go back to start
		
		//test audio long text
		do{
			if(!(fpi.getFormEntryModel().getCaptionPrompt().getFormElement() instanceof QuestionDef)) continue;
			fep = fpi.getFormEntryModel().getQuestionPrompt();
			q = fpi.getCurrentQuestion();
			if(q.getTextID() == null) continue;
			if(q.getTextID().equals("id") && fep.getAvailableTextFormTypes(q.getTextID()).contains("audio")){
				if(!("jr://audio/hah.mp3".equals(fep.getAudioText()))){
					fail("get AudioText() doesn't work.");
				}else{
					foundFlag = true;
				}
			}
		}while(fec.stepToNextEvent()!=FormEntryController.EVENT_END_OF_FORM);
		
		if(!foundFlag) fail("Couldn't find QuestionDef with TextID [id] with available image/audio text form.");
	}
	
	/* TODO
	 * Image uri translation/reference tests (Clayton's stuff)
	 */
	
	@Deprecated
	public void testLocaleChanged () {
		QuestionDef q = new QuestionDef();
		q.setLabelInnerText("zh: some text");
//		q.setShortText("zh: short text");
		q.setHelpText("zh: help text");
		q.setTextID("textID");
//		q.setShortTextID("short text", null);
		q.setHelpTextID("help text");
		q.addSelectChoice(new SelectChoice("choice", "val1"));
		q.addSelectChoice(new SelectChoice("","non-loc: choice", "val2", false));
		
		QuestionObserver qo = new QuestionObserver();
		q.registerStateObserver(qo);
		
		Localizer l = new Localizer();
		TableLocaleSource table = new TableLocaleSource();
		l.addAvailableLocale("en");
		table.setLocaleMapping("textID", "en: some text");
		table.setLocaleMapping("short text", "en: short text");
		table.setLocaleMapping("help text", "en: help text");
		table.setLocaleMapping("choice", "en: choice");
		l.registerLocaleResource("en", table);
		l.setLocale("en");
		
		q.localeChanged("locale", l);
		if (!"en: some text".equals(q.getLabelInnerText()) || !"en: help text".equals(q.getHelpText()) ||
				!"[{choice}en: choice => val1, non-loc: choice => val2]".equals(q.getChoices().toString()) ||
				!qo.flag || qo.flags != FormElementStateListener.CHANGE_LOCALE) {
			fail("Improper locale change update");
		}
	}	


	//test to check if the method for getting labelInnerText when an element
	//is not localized works.
	public void testNonLocalizedText(){
		FormEntryController fec = fpi.getFormEntryController();
		boolean testFlag = false;
		Localizer l = fpi.getFormDef().getLocalizer();
		
		l.setDefaultLocale(l.getAvailableLocales()[0]);
		l.setLocale(l.getAvailableLocales()[0]);
		
		do{
			if(fpi.getCurrentQuestion()==null) continue;
			QuestionDef q = fpi.getCurrentQuestion();
			fep = fpi.getFormEntryModel().getQuestionPrompt();
			String t = fep.getQText();
			if(t==null) continue;
			if(t.equals("Non-Localized label inner text!")) testFlag = true;
			
			
		}while(fec.stepToNextEvent()!=fec.EVENT_END_OF_FORM);
		
		if(!testFlag) fail("Failed to fallback to labelInnerText in testNonLocalizedText()");
	}	
	
	public void testReferences(){
		QuestionDef q = fpi.getFirstQuestionDef();
		FormEntryPrompt fep = fpi.getFormEntryModel().getQuestionPrompt();
		
		Localizer l = fpi.getFormDef().getLocalizer();
		l.setDefaultLocale(l.getAvailableLocales()[0]);
		l.setLocale(l.getAvailableLocales()[0]);
		
		String audioURI = fep.getAudioText();
		String ref;
		
		ReferenceManager._().addReferenceFactory(new ResourceReferenceFactory());
		ReferenceManager._().addRootTranslator(new RootTranslator("jr://audio/", "jr://resource/"));
		try{
			Reference r = ReferenceManager._().DeriveReference(audioURI);
			ref = r.getURI();
			if(!ref.equals("jr://resource/hah.mp3")){
				fail("Root translation failed.");
			}
		}catch(InvalidReferenceException ire){
			fail("There was an Invalid Reference Exception:"+ire.getMessage());
			ire.printStackTrace();
		}
		
		
		ReferenceManager._().addRootTranslator(new RootTranslator("jr://images/","jr://resource/"));
		q = fpi.getNextQuestion();
		fep = fpi.getFormEntryModel().getQuestionPrompt();
		String imURI = fep.getImageText();
		try{
			Reference r = ReferenceManager._().DeriveReference(imURI);
			ref = r.getURI();
			if(!ref.equals("jr://resource/four.gif")){
				fail("Root translation failed.");
			}
		}catch(InvalidReferenceException ire){
			fail("There was an Invalid Reference Exception:"+ire.getMessage());
			ire.printStackTrace();
		}
	}
	
	private class QuestionObserver implements FormElementStateListener {
		public boolean flag = false;
		public TreeElement e;
		public QuestionDef q;
		public int flags;
		
		public void formElementStateChanged (IFormElement q, int flags) {
			flag = true;
			this.q = (QuestionDef)q;
			this.flags = flags;
		}

		public void formElementStateChanged(TreeElement question, int changeFlags) {
			flag = true;
			this.e = question;
			this.flags = changeFlags;
		}
	}

	public QuestionDef getQ() {
		return q;
	}

	public void setQ(QuestionDef q) {
		this.q = q;
	}

	public FormEntryPrompt getFep() {
		return fep;
	}

	public void setFep(FormEntryPrompt fep) {
		this.fep = fep;
	}
}
