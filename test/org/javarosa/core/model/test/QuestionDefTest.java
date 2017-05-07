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

import java.util.ArrayList;

import org.javarosa.core.model.Constants;
import org.javarosa.core.model.FormElementStateListener;
import org.javarosa.core.model.IDataReference;
import org.javarosa.core.model.IFormElement;
import org.javarosa.core.model.QuestionDef;
import org.javarosa.core.model.instance.TreeElement;
import org.javarosa.core.reference.InvalidReferenceException;
import org.javarosa.core.reference.Reference;
import org.javarosa.core.reference.ReferenceManager;
import org.javarosa.core.reference.ResourceReferenceFactory;
import org.javarosa.core.reference.RootTranslator;
import org.javarosa.core.services.PrototypeManager;
import org.javarosa.core.services.locale.Localizer;
import org.javarosa.core.test.FormParseInit;
import org.javarosa.core.util.externalizable.ExtUtil;
import org.javarosa.core.util.externalizable.PrototypeFactory;
import org.javarosa.form.api.FormEntryPrompt;
import org.junit.Before;
import org.junit.Test;

import static junit.framework.TestCase.fail;

public class QuestionDefTest {
    private FormParseInit fpi = null;

    @Before
    public void setUp() {
        fpi = new FormParseInit();
        fpi.getFirstQuestionDef();
    }

    private static final PrototypeFactory pf;

    static {
        PrototypeManager.registerPrototype("org.javarosa.model.xform.XPathReference");
        pf = ExtUtil.defaultPrototypes();
    }

    private void testSerialize(QuestionDef q, String msg) {
        //ExternalizableTest.testExternalizable(q, this, pf, "QuestionDef [" + msg + "]");
    }

    @Test
    public void testConstructors() {
        QuestionDef q;

        q = new QuestionDef();
        if (q.getID() != -1) {
            fail("QuestionDef not initialized properly (default constructor)");
        }

        q = new QuestionDef(17, Constants.CONTROL_RANGE);
        if (q.getID() != 17) {
            fail("QuestionDef not initialized properly");
        }
        testSerialize(q, "b");
    }

    private IDataReference newRef() {
        IDataReference ref = new DummyReference();
        ref.setReference("/data");
        pf.addClass(DummyReference.class);
        return ref;
    }

    @Test
    public void testAccessorsModifiers() {
        QuestionDef q = new QuestionDef();

        q.setID(45);
        if (q.getID() != 45) {
            fail("ID getter/setter broken");
        }
        testSerialize(q, "c");

        IDataReference ref = newRef();
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

    @Test
    public void testChild() {
        QuestionDef q = new QuestionDef();

        if (q.getChildren() != null) {
            fail("Question has children");
        }

        try {
            q.setChildren(new ArrayList<IFormElement>());
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

    @Test
    public void testFlagObservers() {
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

    @Test
    public void testReferences() {
        QuestionDef q = fpi.getFirstQuestionDef();
        FormEntryPrompt fep = fpi.getFormEntryModel().getQuestionPrompt();

        Localizer l = fpi.getFormDef().getLocalizer();
        l.setDefaultLocale(l.getAvailableLocales()[0]);
        l.setLocale(l.getAvailableLocales()[0]);

        String audioURI = fep.getAudioText();
        String ref;

        ReferenceManager.instance().addReferenceFactory(new ResourceReferenceFactory());
        ReferenceManager.instance().addRootTranslator(new RootTranslator("jr://audio/", "jr://resource/"));
        try {
            Reference r = ReferenceManager.instance().DeriveReference(audioURI);
            ref = r.getURI();
            if (!ref.equals("jr://resource/hah.mp3")) {
                fail("Root translation failed.");
            }
        } catch (InvalidReferenceException ire) {
            fail("There was an Invalid Reference Exception:" + ire.getMessage());
            ire.printStackTrace();
        }


        ReferenceManager.instance().addRootTranslator(new RootTranslator("jr://images/", "jr://resource/"));
        q = fpi.getNextQuestion();
        fep = fpi.getFormEntryModel().getQuestionPrompt();
        String imURI = fep.getImageText();
        try {
            Reference r = ReferenceManager.instance().DeriveReference(imURI);
            ref = r.getURI();
            if (!ref.equals("jr://resource/four.gif")) {
                fail("Root translation failed.");
            }
        } catch (InvalidReferenceException ire) {
            fail("There was an Invalid Reference Exception:" + ire.getMessage());
            ire.printStackTrace();
        }
    }

    private class QuestionObserver implements FormElementStateListener {
        boolean flag = false;
        public TreeElement e;
        QuestionDef q;
        int flags;

        @Override
        public void formElementStateChanged(IFormElement q, int flags) {
            flag = true;
            this.q = (QuestionDef) q;
            this.flags = flags;
        }

        @Override
        public void formElementStateChanged(TreeElement question, int changeFlags) {
            flag = true;
            this.e = question;
            this.flags = changeFlags;
        }
    }
}
