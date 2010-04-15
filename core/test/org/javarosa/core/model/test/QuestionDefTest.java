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
import org.javarosa.core.model.IDataReference;
import org.javarosa.core.model.IFormElement;
import org.javarosa.core.model.QuestionDef;
import org.javarosa.core.model.SelectChoice;
import org.javarosa.core.model.instance.TreeElement;
import org.javarosa.core.services.PrototypeManager;
import org.javarosa.core.services.locale.Localizer;
import org.javarosa.core.services.locale.TableLocaleSource;
import org.javarosa.core.util.externalizable.ExtUtil;
import org.javarosa.core.util.externalizable.PrototypeFactory;

public class QuestionDefTest extends TestCase {
	public QuestionDefTest(String name, TestMethod rTestMethod) {
		super(name, rTestMethod);
	}
	
	public QuestionDefTest(String name) {
		super(name);
	}
	
	public QuestionDefTest() {
		super();
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
	
	public final static int NUM_TESTS = 11;
	public void doTest (int i) {
		switch (i) {
		case 1: testConstructors(); break;
		case 2: testAccessorsModifiers(); break;
		case 3: testChild(); break;
		case 4: testFlagObservers(); break;
		case 5: testPromptsNoLocalizer(); break;
		case 6: testPromptIDsNoLocalizer(); break;
		case 7: testPromptsWithLocalizer(); break;
		case 8: testSelectChoicesNoLocalizer(); break;
		case 9: testSelectChoiceIDsNoLocalizer(); break;
		case 10: testLocaleChanged(); break;
		case 11: testLocaleChangedNoLocalizable(); break;
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

//		q.setShortText("short text");
//		if (!"short text".equals(q.getShortText())) {
//			fail("Short text getter/setter broken");
//		}
//		testSerialize(q, "o");
		
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

		q.setHelpTextID("help text id", null);
		if (!"help text id".equals(q.getHelpTextID()) || q.getHelpText() != null) {
			fail("Help text ID getter/setter broken");
		}
	}
	
	public void testPromptsWithLocalizer () {
		QuestionDef q = new QuestionDef();
		
		Localizer l = new Localizer();

		TableLocaleSource table = new TableLocaleSource();
		l.addAvailableLocale("locale");
		table.setLocaleMapping("prompt;long", "loc: long text");
		table.setLocaleMapping("prompt;short", "loc: short text");
		table.setLocaleMapping("help", "loc: help text");
		l.registerLocaleResource("locale", table);
		
		l.setLocale("locale");
		
		q.setTextID("prompt");

		if (!"loc: long text".equals(l.getLocalizedText(q.getTextID()+";" + "long"))) {
			fail("Long text did not localize when setting ID");
		}
		testSerialize(q, "t");
	
		q.setHelpTextID("help", l);
		if (!"loc: help text".equals(q.getHelpText())) {
			fail("Help text did not localize when setting ID");
		}
		testSerialize(q, "v");
	}

	public void testSelectChoicesNoLocalizer () {
		QuestionDef q = new QuestionDef();
		if (q.getNumChoices() != 0) {
			fail("Select choices not empty on init");
		}

		q.addSelectChoice(new SelectChoice("","choice", "val", false));
		q.addSelectChoice(new SelectChoice("","stacey's", "mom", false));
		if (!q.getChoices().toString().equals("[choice => val, stacey's => mom]")) {
			fail("Could not add individual select choice");
		}
		testSerialize(q, "w");
	}
	
	public void testSelectChoiceIDsNoLocalizer () {
		QuestionDef q = new QuestionDef();
		
		q.addSelectChoice(new SelectChoice("choice1 id", "val1"));
		q.addSelectChoice(new SelectChoice("","loc: choice2", "val2", false));
		if (!q.getChoices().toString().equals("[{choice1 id} => val1, loc: choice2 => val2]")) {
			fail("Could not add individual select choice ID");
		}
		testSerialize(q, "y");
	}
	
	public void testLocaleChanged () {
		QuestionDef q = new QuestionDef();
		q.setLabelInnerText("zh: some text");
//		q.setShortText("zh: short text");
		q.setHelpText("zh: help text");
		q.setTextID("textID");
//		q.setShortTextID("short text", null);
		q.setHelpTextID("help text", null);
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


	public void testLocaleChangedNoLocalizable () {
		
		///We really need to rewrite all of this (localization doesn't work this way anymore).
		/////////////////////////////////////////////////////////////////
		
		
		
		
//		QuestionDef q = new QuestionDef();
//		q.setLabelInnerText("long text");
//		q.setHelpText("help text");
//		//choices tested above
//		
//		QuestionObserver qo = new QuestionObserver();
//		q.registerStateObserver(qo);
//		
//		Localizer l = new Localizer();
//		l.addAvailableLocale("locale");
//		l.setLocale("locale");
//		
//		q.localeChanged("locale", l);
//		if (!"long text".equals(q.getLongText()) || !"short text".equals(q.getShortText()) || !"help text".equals(q.getHelpText()) ||
//				!qo.flag || qo.flags != FormElementStateListener.CHANGE_LOCALE) {
//			fail("Improper locale change update (no localizable fields)");
//		}
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
}
