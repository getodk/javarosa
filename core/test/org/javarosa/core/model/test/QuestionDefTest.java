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

import java.util.ArrayList;
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
		System.out.println("Running QuestionDefTest tests...");
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

	public final static int NUM_TESTS = 5;
	public void doTest (int i) {
		switch (i) {
		case 1: testConstructors(); break;
		case 2: testAccessorsModifiers(); break;
		case 3: testChild(); break;
		case 4: testFlagObservers(); break;
		case 5: testReferences(); break;
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
			q.setChildren(new ArrayList<IFormElement>(0));
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














//	//Deprecated
//	public void testLocaleChanged () {
//		QuestionDef q = new QuestionDef();
//		q.setLabelInnerText("zh: some text");
//		q.setHelpText("zh: help text");
//		q.setTextID("textID");
//		q.setHelpTextID("help text");
//		q.addSelectChoice(new SelectChoice("choice", "val1"));
//		q.addSelectChoice(new SelectChoice("","non-loc: choice", "val2", false));
//
//		QuestionObserver qo = new QuestionObserver();
//		q.registerStateObserver(qo);
//
//		Localizer l = new Localizer();
//		TableLocaleSource table = new TableLocaleSource();
//		l.addAvailableLocale("en");
//		table.setLocaleMapping("textID", "en: some text");
//		table.setLocaleMapping("short text", "en: short text");
//		table.setLocaleMapping("help text", "en: help text");
//		table.setLocaleMapping("choice", "en: choice");
//		l.registerLocaleResource("en", table);
//		l.setLocale("en");
//
//		q.localeChanged("locale", l);
//		if (!"en: some text".equals(q.getLabelInnerText()) || !"en: help text".equals(q.getHelpText()) ||
//				!"[{choice}en: choice => val1, non-loc: choice => val2]".equals(q.getChoices().toString()) ||
//				!qo.flag || qo.flags != FormElementStateListener.CHANGE_LOCALE) {
//			fail("Improper locale change update");
//		}
//	}




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
