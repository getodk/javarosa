/*
 * Copyright 2019 Nafundi
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.javarosa.core.test;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.nio.file.Files.createTempDirectory;
import static java.nio.file.Files.createTempFile;
import static java.nio.file.Files.delete;
import static java.nio.file.Files.newInputStream;
import static java.nio.file.Files.newOutputStream;
import static java.nio.file.Files.write;
import static java.nio.file.StandardOpenOption.CREATE;
import static org.javarosa.form.api.FormEntryController.EVENT_BEGINNING_OF_FORM;
import static org.javarosa.form.api.FormEntryController.EVENT_END_OF_FORM;
import static org.javarosa.form.api.FormEntryController.EVENT_GROUP;
import static org.javarosa.form.api.FormEntryController.EVENT_PROMPT_NEW_REPEAT;
import static org.javarosa.form.api.FormEntryController.EVENT_QUESTION;
import static org.javarosa.form.api.FormEntryController.EVENT_REPEAT;
import static org.javarosa.form.api.FormEntryController.EVENT_REPEAT_JUNCTURE;
import static org.javarosa.test.utils.ResourcePathHelper.r;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.javarosa.core.model.CoreModelModule;
import org.javarosa.core.model.FormDef;
import org.javarosa.core.model.FormIndex;
import org.javarosa.core.model.IFormElement;
import org.javarosa.core.model.ItemsetBinding;
import org.javarosa.core.model.QuestionDef;
import org.javarosa.core.model.SelectChoice;
import org.javarosa.core.model.condition.EvaluationContext;
import org.javarosa.core.model.data.IAnswerData;
import org.javarosa.core.model.data.IntegerData;
import org.javarosa.core.model.data.MultipleItemsData;
import org.javarosa.core.model.data.StringData;
import org.javarosa.core.model.data.helper.Selection;
import org.javarosa.core.model.instance.InstanceInitializationFactory;
import org.javarosa.core.model.instance.TreeElement;
import org.javarosa.core.model.instance.TreeReference;
import org.javarosa.core.services.PrototypeManager;
import org.javarosa.core.util.JavaRosaCoreModule;
import org.javarosa.core.util.XFormsElement;
import org.javarosa.core.util.externalizable.DeserializationException;
import org.javarosa.debug.Event;
import org.javarosa.form.api.FormEntryController;
import org.javarosa.form.api.FormEntryModel;
import org.javarosa.form.api.FormEntryPrompt;
import org.javarosa.model.xform.XFormsModule;
import org.javarosa.xpath.XPathParseTool;
import org.javarosa.xpath.expr.XPathNumericLiteral;
import org.javarosa.xpath.expr.XPathPathExpr;
import org.javarosa.xpath.parser.XPathSyntaxException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <div style="border: 1px 1px 1px 1px; background-color: #556B2F; color: white; padding: 20px">
 * <b>Warning</b> This class is probably incomplete. If your testing requirements
 * aren't met by this class, please, ask around and let's try to make this tool
 * awesome together.
 * <ul>
 * <li><a href="https://opendatakit.slack.com">OpenDataKit Slack</a></li>
 * <li><a href="https://github.com/opendatakit/javarosa/issues">GitHub issues</a></li>
 * <li><a href="https://forum.opendatakit.org/c/development">Development forum</a></li>
 * </ul>
 * <hr/>
 * </div>
 * <p>
 * This class helps writing JavaRosa tests. It provides two separate APIs:
 * <ul>
 * <li>A static, declarative API that lets the test author to define the state
 * of a form in a given time.</li>
 * <li>A dynamic, imperative API that lets the test author fill the form as the
 * user would do, by controlling the flow while filling questions. These methods
 * return the {@link Scenario} to be able to chain compatible methods.</li>
 * </ul>
 * <p>
 * All the methods that accept a {@link String} xpath argument, support an enhanced
 * version of xpath with the following perks and limitations:
 * <ul>
 * <li>Only supports absolute xpaths</li>
 * <li>Supports adding the index (zero-indexed) of a repeat instance by suffixing it between
 * brackets. Example that would select the fourth instance of the <code>/foo/bar</code>
 * repeat: <code>/foo/bar[3]</code></li>
 * </ul>
 * <p>
 */
// TODO Extract both APIs to two separate contexts so that they can't be mixed, probably best if it's a Scenario steps runner that would have the common .given(setup).when(action).then(assertion)
public class Scenario {
    private static final Logger log = LoggerFactory.getLogger(Scenario.class);
    private final FormDef formDef;
    private final FormEntryController formEntryController;

    private Scenario(FormDef formDef, FormEntryController formEntryController) {
        this.formDef = formDef;
        this.formEntryController = formEntryController;
    }

    // region Miscelaneous

    public static TreeReference getRef(String xpath) {
        try {
            TreeReference reference = ((XPathPathExpr) XPathParseTool.parseXPath(xpath)).getReference();
            for (int i = 0; i < reference.size(); i++)
                if (reference.getPredicate(i) != null
                    && !reference.getPredicate(i).isEmpty()
                    && reference.getPredicate(i).get(0) instanceof XPathNumericLiteral
                ) {
                    int multiplicity = Double.valueOf(((XPathNumericLiteral) reference.getPredicate(i).get(0)).d).intValue();
                    reference.setMultiplicity(i, multiplicity);
                    reference = reference.removePredicates(i);
                }
            return reference;
        } catch (XPathSyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    public void newInstance() {
        formDef.initialize(true, new InstanceInitializationFactory());
    }

    public void setLanguage(String language) {
        formEntryController.setLanguage(language);
    }

    public EvaluationContext getEvaluationContext() {
        return formDef.getEvaluationContext();
    }

    public Scenario populateDynamicChoices() {
        FormIndex backupIndex = formEntryController.getModel().getFormIndex();
        while (!atTheEndOfForm()) {
            silentNext();
            FormIndex currentIndex = formEntryController.getModel().getFormIndex();
            if (isQuestionAtIndex()) {
                QuestionDef questionAtIndex = getQuestionAtIndex();
                ItemsetBinding dynamicChoices = questionAtIndex.getDynamicChoices();
                if (dynamicChoices != null)
                    formDef.populateDynamicChoices(dynamicChoices, currentIndex.getReference());
            }
        }
        silentJump(backupIndex);
        return this;
    }

    public Scenario onDagEvent(Consumer<Event> callback) {
        formDef.setEventNotifier(callback::accept);
        return this;
    }

    public Scenario serializeAndDeserializeForm() throws IOException, DeserializationException {
        // Initialize serialization
        PrototypeManager.registerPrototypes(JavaRosaCoreModule.classNames);
        PrototypeManager.registerPrototypes(CoreModelModule.classNames);
        new XFormsModule().registerModule();

        // Serialize form in a temp file
        Path tempFile = createTempFile("javarosa", "test");
        formDef.writeExternal(new DataOutputStream(newOutputStream(tempFile)));

        // Create an empty FormDef and deserialize the form into it
        FormDef deserializedFormDef = new FormDef();
        deserializedFormDef.readExternal(
            new DataInputStream(newInputStream(tempFile)),
            PrototypeManager.getDefault()
        );

        delete(tempFile);
        return new Scenario(deserializedFormDef, new FormEntryController(new FormEntryModel(deserializedFormDef)));
    }

    // endregion

    // region Initialization of a Scenario

    public static Scenario init(String formName, XFormsElement form) throws IOException {
        Path formFile = createTempDirectory("javarosa").resolve(formName + ".xml");
        String xml = form.asXml();
        System.out.println(xml);
        write(formFile, xml.getBytes(UTF_8), CREATE);
        return Scenario.init(formFile);
    }

    public static Scenario init(String formFileName) {
        return init(r(formFileName));
    }

    public static Scenario init(Path formFile) {
        // TODO explain why this sequence of calls
        new XFormsModule().registerModule();
        FormParseInit fpi = new FormParseInit(formFile);
        FormDef formDef = fpi.getFormDef();
        formDef.initialize(true, new InstanceInitializationFactory());
        FormEntryModel formEntryModel = new FormEntryModel(formDef);
        FormEntryController formEntryController = new FormEntryController(formEntryModel);
        return new Scenario(formDef, formEntryController);
    }

    // endregion

    // region Answer a specific question

    /**
     * Answers with a string value the question at the form index
     * corresponding to the provided reference.
     * <p>
     * This method has side-effects:
     * - It will create all the required middle and end repeat group instances
     * - It changes the current form index
     */
    // TODO Make more of these, one for each data type, and use the correct IAnswerData type
    public AnswerResult answer(String xPath, String value) {
        createMissingRepeats(xPath);
        TreeReference ref = getRef(xPath);
        silentJump(getIndexOf(ref));
        return answer(value);
    }

    /**
     * Answers with a list of string values the question at the form index
     * corresponding to the provided reference.
     * <p>
     * This method has side-effects:
     * - It will create all the required middle and end repeat group instances
     * - It changes the current form index
     */
    // TODO Make more of these, one for each data type, and use the correct IAnswerData type
    public AnswerResult answer(String xPath, String... selectionValues) {
        createMissingRepeats(xPath);
        TreeReference ref = getRef(xPath);
        silentJump(getIndexOf(ref));
        return answer(Arrays.asList(selectionValues));
    }

    /**
     * Answers with an integer value the question at the form index
     * corresponding to the provided reference.
     * <p>
     * This method has side-effects:
     * - It will create all the required middle and end repeat group instances
     * - It changes the current form index
     */
    // TODO Make more of these, one for each data type, and use the correct IAnswerData type
    public AnswerResult answer(String xPath, int value) {
        createMissingRepeats(xPath);
        TreeReference ref = getRef(xPath);
        silentJump(getIndexOf(ref));
        return answer(value);
    }

    // endregion

    // region Answer the question at the form index

    private AnswerResult answer(IAnswerData data) {
        FormIndex formIndex = formEntryController.getModel().getFormIndex();
        log.info("Answer {} at {}", data, formIndex.getReference());
        return AnswerResult.from(formEntryController.answerQuestion(formIndex, data, true));
    }

    /**
     * Answers the question at the form index
     */
    // TODO Make more of these, one for each data type, and use the correct IAnswerData type
    public AnswerResult answer(String value) {
        return answer(new StringData(value));
    }

    /**
     * Answers the question at the form index
     */
    // TODO Make more of these, one for each data type, and use the correct IAnswerData type
    public AnswerResult answer(List<String> values) {
        return answer(new MultipleItemsData(values.stream().map(Selection::new).collect(Collectors.toList())));
    }

    /**
     * Answers the question at the form index
     */
    // TODO Make more of these, one for each data type, and use the correct IAnswerData type
    public AnswerResult answer(int value) {
        return answer(new IntegerData(value));
    }

    /**
     * Answers the question at the form index
     */
    // TODO Make more of these, one for each data type, and use the correct IAnswerData type
    public AnswerResult answer(char value) {
        return answer(new StringData(String.valueOf(value)));
    }

    // endregion

    // region Repeat group manipulation

    public void createMissingRepeats(String xPath) {
        FormIndex backupIndex = formEntryController.getModel().getFormIndex();
        TreeReference reference = getRef(xPath);
        for (int i = 0; i < reference.size(); i++) {
            if (reference.getMultiplicity(i) < 0)
                // This part has no multiplicity, so we don't need to do anything.
                continue;

            // Search for the subreference among the form indexes
            TreeReference subReference = reference.getSubReference(i);
            silentJump(FormIndex.createBeginningOfFormIndex());
            while (!atTheEndOfForm() && formDef.getMainInstance().resolveReference(subReference) == null)
                if (silentNext() == EVENT_PROMPT_NEW_REPEAT) {
                    // Use the string representation to avoid issues with numeric
                    // literal predicates corresponding to the multiplicity index
                    // that the xpath parser might create in tree references
                    TreeReference refAtIndex = formEntryController.getModel().getFormIndex().getReference();
                    if (refAtIndex.toString().equals(subReference.toString()))
                        // We're in one (probably the first) of the siblings of the
                        // repeat group at the subRef. Create new repeats until the
                        // one want we need is created
                        while (formDef.getMainInstance().resolveReference(subReference) == null)
                            formEntryController.descendIntoNewRepeat();
                }
            if (formDef.getMainInstance().resolveReference(subReference) == null)
                throw new RuntimeException("We couldn't create missing repeats. Check your form and your test");
        }
        silentJump(backupIndex);
    }

    public Scenario removeRepeat(String xPath) {
        TreeReference reference = getRef(xPath);
        List<TreeReference> treeReferences = formDef.getEvaluationContext().expandReference(reference);
        if (treeReferences.size() > 1)
            throw new RuntimeException("Provided xPath expands to more than one reference");

        TreeElement group = formDef.getMainInstance().resolveReference(reference);
        FormIndex childIndex = null;
        for (int i = 0; i < group.getNumChildren(); i++) {
            childIndex = getIndexOf(group.getChildAt(i).getRef());
            if (childIndex != null)
                break;
        }
        if (childIndex == null)
            throw new RuntimeException("Can't find an index inside the repeat group you want to remove. Please add some field and a form control.");

        // FormDef.deleteRepeat requires a FormIndex belonging to
        // a descendant of the repeat we want to delete
        formDef.deleteRepeat(childIndex);
        return this;
    }

    public void createNewRepeat() {
        log.info("Create repeat instance {}", formEntryController.getModel().getFormIndex().getReference());
        formEntryController.newRepeat();
    }

    // endregion

    // region Traversing the form

    /**
     * Position the form index on the next event.
     * <p>
     * This method will leave a trace on the logs.
     */
    public int next() {
        int jumpResultCode = formEntryController.stepToNextEvent();
        String jumpResult = decodeJumpResult(jumpResultCode);
        FormIndex formIndex = formEntryController.getModel().getFormIndex();
        IFormElement child = formDef.getChild(formIndex);
        String labelInnerText = Optional.ofNullable(child.getLabelInnerText()).map(s -> " " + s).orElse("");
        String textId = Optional.ofNullable(child.getTextID()).map(s -> " itext:" + s).orElse("");
        String referenceAtIndex = Optional.ofNullable(formIndex.getReference()).map(ref -> ref.toString(true, true)).orElse("");
        log.info("Jump to {}{}{}{}", jumpResult, labelInnerText, textId, referenceAtIndex);
        return jumpResultCode;
    }

    public void jumpToBeginningOfForm() {
        jump(FormIndex.createBeginningOfFormIndex());
    }

    private int silentNext() {
        return formEntryController.stepToNextEvent();
    }

    private void jump(FormIndex indexOf) {
        int jumpResultCode = formEntryController.jumpToIndex(indexOf);
        log.debug("Jumped to " + decodeJumpResult(jumpResultCode));
    }

    private void silentJump(FormIndex indexOf) {
        formEntryController.jumpToIndex(indexOf);
    }

    private String decodeJumpResult(int code) {
        switch (code) {
            case EVENT_BEGINNING_OF_FORM:
                return "Beginning of Form";
            case EVENT_END_OF_FORM:
                return "End of Form";
            case EVENT_PROMPT_NEW_REPEAT:
                return "Prompt new Repeat";
            case EVENT_QUESTION:
                return "Question";
            case EVENT_GROUP:
                return "Group";
            case EVENT_REPEAT:
                return "Repeat";
            case EVENT_REPEAT_JUNCTURE:
                return "Repeat Juncture";
        }
        return "Unknown";
    }

    // endregion

    // region Inspect the form index

    public boolean atTheEndOfForm() {
        return formEntryController.getModel().getFormIndex().isEndOfFormIndex();
    }

    public QuestionDef getQuestionAtIndex() {
        return formEntryController.getModel().getQuestionPrompt().getQuestion();
    }

    public boolean isQuestionAtIndex() {
        try {
            getQuestionAtIndex();
            return true;
        } catch (RuntimeException e) {
            return false;
        }
    }

    // endregion

    // region Inspect the main instance

    /**
     * Returns the value of the element located at the given xPath in the main instance.
     */
    @SuppressWarnings("unchecked")
    public <T extends IAnswerData> T answerOf(String xPath) {
        TreeReference reference = getRef(xPath);
        List<TreeReference> treeReferences = formDef.getEvaluationContext().expandReference(reference);
        if (treeReferences.size() > 1)
            throw new RuntimeException("Provided xPath expands to more than one reference");

        TreeElement element = formDef.getMainInstance().resolveReference(reference);
        return element != null ? (T) element.getValue() : null;
    }

    public List<TreeElement> repeatInstancesOf(String xPath) {
        TreeReference reference = getRef(xPath);
        if (!reference.isAmbiguous())
            throw new RuntimeException("Provided xPath must be ambiguous");

        return formDef.getEvaluationContext()
            .expandReference(reference)
            .stream()
            .map(ref -> formDef.getMainInstance().resolveReference(ref)).collect(Collectors.toList());
    }

    /**
     * Returns the list of choices of the &lt;select&gt; or &lt;select1&gt; form controls.
     * This method ensures that any dynamic choice lists are populated to reflect the status
     * of the form (already answered questions, etc.).
     */
    public List<SelectChoice> choicesOf(String xPath) {
        TreeReference reference = getRef(xPath);
        List<TreeReference> treeReferences = formDef.getEvaluationContext().expandReference(reference);
        if (treeReferences.size() > 1)
            throw new RuntimeException("Provided xPath expands to more than one reference");

        FormEntryPrompt questionPrompt = formEntryController.getModel().getQuestionPrompt(getIndexOf(reference));
        // This call triggers the correct population of dynamic choices.
        questionPrompt.getAnswerValue();
        QuestionDef control = questionPrompt.getQuestion();
        return control.getChoices() == null
            // If the (static) choices is null, that means there is an itemset and choices are dynamic
            // ItemsetBinding.getChoices() will work because we've called questionPrompt.getAnswerValue()
            ? control.getDynamicChoices().getChoices()
            : control.getChoices();
    }

    // endregion

    private FormIndex getIndexOf(TreeReference ref) {
        // Get an unambiguous ref in case we can expand the provided
        // ref to exactly one ref. This will adapt incoming /data/some-field
        // to /data/some-field[0]. Otherwise, unbound refs won't match existing
        // form indexes.
        TreeReference unambiguousRef = ref;
        List<TreeReference> refs = formDef.getEvaluationContext().expandReference(ref);
        if (refs.size() == 1)
            unambiguousRef = refs.get(0);
        FormEntryModel model = formEntryController.getModel();
        FormIndex backupIndex = model.getFormIndex();
        jump(FormIndex.createBeginningOfFormIndex());
        FormIndex index = model.getFormIndex();
        do {
            TreeReference reference = index.getReference();
            if (reference != null && reference.equals(unambiguousRef)) {
                jump(backupIndex);
                return index;
            }
            index = model.incrementIndex(index);
        } while (index.isInForm());
        jump(backupIndex);
        return null;
    }

    public enum AnswerResult {
        OK(0), REQUIRED_BUT_EMPTY(1), CONSTRAINT_VIOLATED(2);

        private final int jrCode;

        AnswerResult(int jrCode) {
            this.jrCode = jrCode;
        }

        public static AnswerResult from(int jrCode) {
            return Stream.of(values())
                .filter(v -> v.jrCode == jrCode)
                .findFirst()
                .orElseThrow(RuntimeException::new);
        }
    }
}
