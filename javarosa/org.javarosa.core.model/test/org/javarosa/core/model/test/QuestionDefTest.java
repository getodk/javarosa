package org.javarosa.core.model.test;

import j2meunit.framework.Test;
import j2meunit.framework.TestCase;
import j2meunit.framework.TestMethod;
import j2meunit.framework.TestSuite;

import java.util.Vector;

import org.javarosa.core.JavaRosaServiceProvider;
import org.javarosa.core.model.Constants;
import org.javarosa.core.model.FormElementStateListener;
import org.javarosa.core.model.IDataReference;
import org.javarosa.core.model.IFormElement;
import org.javarosa.core.model.QuestionDef;
import org.javarosa.core.model.instance.TreeElement;
import org.javarosa.core.model.utils.Localizer;
import org.javarosa.core.util.OrderedHashtable;
import org.javarosa.core.util.externalizable.ExtUtil;
import org.javarosa.core.util.externalizable.PrototypeFactory;
import org.javarosa.core.util.test.ExternalizableTest;

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
		JavaRosaServiceProvider.instance().registerPrototype("org.javarosa.model.xform.XPathReference");
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
		ExternalizableTest.testExternalizable(q, this, pf, "QuestionDef [" + msg + "]");
	}
	
	public final static int NUM_TESTS = 15;
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
		case 10: testLocalizeSelectMap(); break;
		case 11: testLocalizeSelectMapNoLocalizer(); break;
		case 12: testLocalizeSelectMapEmpty(); break;
		case 13: testSelectChoiceIDsWithLocalizer(); break;
		case 14: testLocaleChanged(); break;
		case 15: testLocaleChangedNoLocalizable(); break;
		}
	}
	
	public void testConstructors () {
		QuestionDef q;
		
		q = new QuestionDef();
		if (q.getID() != -1 || q.getTitle() != null) {
			fail("QuestionDef not initialized properly (default constructor)");
		}
		testSerialize(q, "a");
		
		q = new QuestionDef(17, "test question", Constants.CONTROL_RANGE);
		if (q.getID() != 17 || !"test question".equals(q.getTitle())) {
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

		q.setTitle("rosebud");
		if (!"rosebud".equals(q.getTitle())) {
			fail("Name getter/setter broken");
		}
		testSerialize(q, "d");

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
		
		q.setLongText("long text");
		if (!"long text".equals(q.getLongText())) {
			fail("Long text getter/setter broken");
		}
		testSerialize(q, "n");

		q.setShortText("short text");
		if (!"short text".equals(q.getShortText())) {
			fail("Short text getter/setter broken");
		}
		testSerialize(q, "o");
		
		q.setHelpText("help text");
		if (!"help text".equals(q.getHelpText())) {
			fail("Help text getter/setter broken");
		}
		testSerialize(q, "p");
	}
	
	public void testPromptIDsNoLocalizer () {
		QuestionDef q = new QuestionDef();
		
		q.setLongTextID("long text id", null);
		if (!"long text id".equals(q.getLongTextID()) || q.getLongText() != null) {
			fail("Long text ID getter/setter broken");
		}
		testSerialize(q, "q");

		q.setShortTextID("short text id", null);
		if (!"short text id".equals(q.getShortTextID()) || q.getShortText() != null) {
			fail("Short text ID getter/setter broken");
		}
		testSerialize(q, "r");

		q.setHelpTextID("help text id", null);
		if (!"help text id".equals(q.getHelpTextID()) || q.getHelpText() != null) {
			fail("Help text ID getter/setter broken");
		}
		testSerialize(q, "s");
	}
	
	public void testPromptsWithLocalizer () {
		QuestionDef q = new QuestionDef();
		
		Localizer l = new Localizer();
		l.addAvailableLocale("locale");
		l.setLocaleMapping("locale", "prompt;long", "loc: long text");
		l.setLocaleMapping("locale", "prompt;short", "loc: short text");
		l.setLocaleMapping("locale", "help", "loc: help text");
		l.setLocale("locale");
		
		q.setLongTextID("prompt;long", l);
		if (!"loc: long text".equals(q.getLongText())) {
			fail("Long text did not localize when setting ID");
		}
		testSerialize(q, "t");
	
		q.setShortTextID("prompt;short", l);
		if (!"loc: short text".equals(q.getShortText())) {
			fail("Short text did not localize when setting ID");
		}
		testSerialize(q, "u");
	
		q.setHelpTextID("help", l);
		if (!"loc: help text".equals(q.getHelpText())) {
			fail("Help text did not localize when setting ID");
		}
		testSerialize(q, "v");
	}

	public void testSelectChoicesNoLocalizer () {
		QuestionDef q = new QuestionDef();
		if (q.getSelectItems() != null || q.getSelectItemIDs() != null || q.getSelectItemsLocalizable() != null) {
			fail("Select choices not null on init");
		}

		q.addSelectItem("choice", "val");
		q.addSelectItem("stacey's", "mom");
		if (!q.getSelectItems().toString().equals("[choice => val, stacey's => mom]")) {
			fail("Could not add individual select choice");
		}
		//won't work: testSerialize(q, "w");
		
		OrderedHashtable newChoices = new OrderedHashtable();
		newChoices.put("alpha", "beta");
		q.setSelectItems(newChoices);
		if (q.getSelectItems() != newChoices) {
			fail("Could not set select choices en masse");
		}
		//won't work: testSerialize(q, "x");
	}
	
	public void testSelectChoiceIDsNoLocalizer () {
		QuestionDef q = new QuestionDef();
		
		q.addSelectItemID("choice1 id", true, "val1");
		q.addSelectItemID("loc: choice2", false, "val2");
		if (!q.getSelectItemIDs().toString().equals("[choice1 id => val1, loc: choice2 => val2]") ||
			!q.getSelectItemsLocalizable().toString().equals("[true, false]") ||
			q.getSelectItems() != null) {
			fail("Could not add individual select choice ID");
		}
		testSerialize(q, "y");

		OrderedHashtable newChoiceIDs = new OrderedHashtable();
		Vector newChoiceLocs = new Vector();
		newChoiceIDs.put("alpha", "beta");
		newChoiceLocs.addElement(Boolean.TRUE);
		q.setSelectItemIDs(newChoiceIDs, newChoiceLocs, null);
		if (q.getSelectItemIDs() != newChoiceIDs || q.getSelectItemsLocalizable() != newChoiceLocs || q.getSelectItems() != null) {
			fail("Could not set select choices en masse");
		}
		testSerialize(q, "z");
	}
	
	public void testLocalizeSelectMap () {
		QuestionDef q = new QuestionDef();

		q.addSelectItem("i", "will");
		q.addSelectItem("be", "overwritten");
		
		Localizer l = new Localizer();
		l.addAvailableLocale("locale");
		l.setLocaleMapping("locale", "choice1", "loc: choice1");
		l.setLocaleMapping("locale", "choice2", "loc: choice2");
		l.setLocale("locale");
		
		q.addSelectItemID("choice1", true, "val1");
		q.addSelectItemID("choice2", true, "val2");
		q.addSelectItemID("non-loc: choice3", false, "val3");
		
		q.localizeSelectMap(l);
		if (!q.getSelectItems().toString().equals("[loc: choice1 => val1, loc: choice2 => val2, non-loc: choice3 => val3]")) {
			fail("Did not localize select choices properly");
		}
		testSerialize(q, "aa");
	}
		
	public void testLocalizeSelectMapNoLocalizer () {
		QuestionDef q = new QuestionDef();

		q.addSelectItemID("choice1", true, "val1");
		q.addSelectItemID("non-loc: choice2", false, "val2");
		
		q.localizeSelectMap(null);
		if (!q.getSelectItems().toString().equals("[[itext:0] => val1, non-loc: choice2 => val2]")) {
			//fail("Did not localize select choices properly (w/o localizer). Given choices = " + q.getSelectItems().toString());
			fail(q.getSelectItems().toString());
		}
		testSerialize(q, "ab");
	}

	public void testLocalizeSelectMapEmpty () {
		QuestionDef q = new QuestionDef();
		Localizer l = new Localizer();
		
		try {
			q.localizeSelectMap(l);
			fail("Did not get exception when localizing null choices");
		} catch (NullPointerException npe) {
			//expected
		}
		
		q.setSelectItemIDs(new OrderedHashtable(), new Vector(), null);
		q.localizeSelectMap(l);
		if (q.getSelectItems() != null) {
			fail("Localized select choices out of nowhere");
		}
		testSerialize(q, "ac");
	}

	public void testSelectChoiceIDsWithLocalizer () {
		QuestionDef q = new QuestionDef();

		Localizer l = new Localizer();
		l.addAvailableLocale("locale");
		l.setLocaleMapping("locale", "choice1", "loc: choice1");
		l.setLocaleMapping("locale", "choice2", "loc: choice2");
		l.setLocale("locale");
		
		OrderedHashtable choiceIDs = new OrderedHashtable();
		Vector choiceLocs = new Vector();
		choiceIDs.put("choice1", "val1");
		choiceLocs.addElement(Boolean.TRUE);
		choiceIDs.put("choice2", "val2");
		choiceLocs.addElement(Boolean.TRUE);
		choiceIDs.put("non-loc: choice3", "val3");
		choiceLocs.addElement(Boolean.FALSE);
		q.setSelectItemIDs(choiceIDs, choiceLocs, l);
		if (q.getSelectItemIDs() != choiceIDs || q.getSelectItemsLocalizable() != choiceLocs ||
				!q.getSelectItems().toString().equals("[loc: choice1 => val1, loc: choice2 => val2, non-loc: choice3 => val3]")) {
			fail("Could not set and localize select choices en masse");
		}
		testSerialize(q, "ad");
	}		
	
	public void testLocaleChanged () {
		QuestionDef q = new QuestionDef();
		q.setLongText("zh: long text");
		q.setShortText("zh: short text");
		q.setHelpText("zh: help text");
		q.addSelectItem("zh: choice", "val");
		q.setLongTextID("long text", null);
		q.setShortTextID("short text", null);
		q.setHelpTextID("help text", null);
		q.addSelectItemID("choice", true, "val");
		
		QuestionObserver qo = new QuestionObserver();
		q.registerStateObserver(qo);
		
		Localizer l = new Localizer();
		l.addAvailableLocale("en");
		l.setLocaleMapping("en", "long text", "en: long text");
		l.setLocaleMapping("en", "short text", "en: short text");
		l.setLocaleMapping("en", "help text", "en: help text");
		l.setLocaleMapping("en", "choice", "en: choice");
		l.setLocale("en");
		
		q.localeChanged("locale", l);
		if (!"en: long text".equals(q.getLongText()) || !"en: short text".equals(q.getShortText()) || !"en: help text".equals(q.getHelpText()) ||
				!"[en: choice => val]".equals(q.getSelectItems().toString()) ||
				!qo.flag || qo.flags != FormElementStateListener.CHANGE_LOCALE) {
			fail("Improper locale change update");
		}
	}	

	public void testLocaleChangedNoLocalizable () {
		QuestionDef q = new QuestionDef();
		q.setLongText("long text");
		q.setShortText("short text");
		q.setHelpText("help text");
		q.addSelectItem("choice", "val");
		
		QuestionObserver qo = new QuestionObserver();
		q.registerStateObserver(qo);
		
		Localizer l = new Localizer();
		l.addAvailableLocale("locale");
		l.setLocale("locale");
		
		q.localeChanged("locale", l);
		if (!"long text".equals(q.getLongText()) || !"short text".equals(q.getShortText()) || !"help text".equals(q.getHelpText()) ||
				!"[choice => val]".equals(q.getSelectItems().toString()) ||
				!qo.flag || qo.flags != FormElementStateListener.CHANGE_LOCALE) {
			fail("Improper locale change update (no localizable fields)");
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
}
