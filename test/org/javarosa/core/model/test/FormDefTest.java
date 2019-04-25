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

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.javarosa.core.model.FormIndex;
import org.javarosa.core.model.IDataReference;
import org.javarosa.core.model.QuestionDef;
import org.javarosa.core.model.data.IntegerData;
import org.javarosa.core.services.PrototypeManager;
import org.javarosa.core.test.FormParseInit;
import org.javarosa.core.util.externalizable.ExtUtil;
import org.javarosa.core.util.externalizable.PrototypeFactory;
import org.javarosa.form.api.FormEntryController;
import org.javarosa.form.api.FormEntryPrompt;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * See testAnswerConstraint() for an example of how to write the 
 * constraint unit type tests.
 * @author adewinter
 *
 */
public class FormDefTest extends TestCase {
    private static final Logger logger = LoggerFactory.getLogger(FormDefTest.class);
    QuestionDef q = null;
    FormEntryPrompt fep = null;
    FormParseInit fpi = null;

    public FormDefTest(String name) {
        super(name);
        logger.info("Running {} test: {}...", this.getClass().getName(), name);
    }

    public void setUp(){
        fpi = new FormParseInit();
        q = fpi.getFirstQuestionDef();
        fep = new FormEntryPrompt(fpi.getFormDef(), fpi.getFormEntryModel().getFormIndex());
    }

    static PrototypeFactory pf;

    static {
        PrototypeManager.registerPrototype("org.javarosa.model.xform.XPathReference");
        pf = ExtUtil.defaultPrototypes();
    }

    public static Test suite() {
        TestSuite aSuite = new TestSuite();

        for (int i = 1; i <= NUM_TESTS; i++) {
            final int testID = i;
            aSuite.addTest(new FormDefTest(doTest(testID)));
        }

        return aSuite;
    }

    private void testSerialize (QuestionDef q, String msg) {
        //ExternalizableTest.testExternalizable(q, this, pf, "QuestionDef [" + msg + "]");
    }

    public final static int NUM_TESTS = 1;

    public static String doTest (int i) {
        switch (i) {
        case 1: return "testAnswerConstraint";
        }
        throw new IllegalStateException("Unexpected index");
    }


    public void testAnswerConstraint(){
        IntegerData ans = new IntegerData(13);
        FormEntryController fec = fpi.getFormEntryController();
        fec.jumpToIndex(FormIndex.createBeginningOfFormIndex());

        do{

            QuestionDef q = fpi.getCurrentQuestion();
            if(q==null || q.getTextID() == null || q.getTextID().length() == 0)continue;
            if(q.getTextID().equals("constraint-test")){
                int response = fec.answerQuestion(ans, true);
                if(response == FormEntryController.ANSWER_CONSTRAINT_VIOLATED){
                    fail("Answer Constraint test failed.");
                }else if(response == FormEntryController.ANSWER_OK){
                    break;
                }else{
                    fail("Bad response from fec.answerQuestion()");
                }
            }
        }while(fec.stepToNextEvent()!=FormEntryController.EVENT_END_OF_FORM);
    }

    public IDataReference newRef(String xpath){
            IDataReference ref = new DummyReference();
            ref.setReference(xpath);
            pf.addClass(DummyReference.class);
            return ref;
    }

}
