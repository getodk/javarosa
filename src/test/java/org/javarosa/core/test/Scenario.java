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

import org.javarosa.core.model.CoreModelModule;
import org.javarosa.core.model.FormDef;
import org.javarosa.core.model.FormIndex;
import org.javarosa.core.model.IFormElement;
import org.javarosa.core.model.QuestionDef;
import org.javarosa.core.model.SelectChoice;
import org.javarosa.core.model.ValidateOutcome;
import org.javarosa.core.model.condition.EvaluationContext;
import org.javarosa.core.model.data.BooleanData;
import org.javarosa.core.model.data.DateData;
import org.javarosa.core.model.data.IAnswerData;
import org.javarosa.core.model.data.IntegerData;
import org.javarosa.core.model.data.MultipleItemsData;
import org.javarosa.core.model.data.SelectOneData;
import org.javarosa.core.model.data.StringData;
import org.javarosa.core.model.data.helper.Selection;
import org.javarosa.core.model.instance.FormInstance;
import org.javarosa.core.model.instance.InstanceInitializationFactory;
import org.javarosa.core.model.instance.TreeElement;
import org.javarosa.core.model.instance.TreeReference;
import org.javarosa.core.services.PrototypeManager;
import org.javarosa.core.services.locale.Localizer;
import org.javarosa.core.services.storage.StorageManager;
import org.javarosa.core.services.storage.util.DummyIndexedStorageUtility;
import org.javarosa.core.util.JavaRosaCoreModule;
import org.javarosa.core.util.XFormsElement;
import org.javarosa.core.util.externalizable.DeserializationException;
import org.javarosa.debug.Event;
import org.javarosa.form.api.FormEntryController;
import org.javarosa.form.api.FormEntryModel;
import org.javarosa.form.api.FormEntryPrompt;
import org.javarosa.model.xform.XFormSerializingVisitor;
import org.javarosa.model.xform.XFormsModule;
import org.javarosa.xform.parse.XFormParser;
import org.javarosa.xpath.XPathParseTool;
import org.javarosa.xpath.expr.XPathExpression;
import org.javarosa.xpath.expr.XPathNumNegExpr;
import org.javarosa.xpath.expr.XPathNumericLiteral;
import org.javarosa.xpath.expr.XPathPathExpr;
import org.javarosa.xpath.parser.XPathSyntaxException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Path;
import java.sql.Date;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.nio.file.Files.createTempDirectory;
import static java.nio.file.Files.createTempFile;
import static java.nio.file.Files.delete;
import static java.nio.file.Files.newInputStream;
import static java.nio.file.Files.newOutputStream;
import static java.nio.file.Files.write;
import static java.nio.file.StandardOpenOption.CREATE;
import static java.util.stream.Collectors.joining;
import static org.javarosa.core.model.instance.TreeReference.INDEX_TEMPLATE;
import static org.javarosa.form.api.FormEntryController.EVENT_BEGINNING_OF_FORM;
import static org.javarosa.form.api.FormEntryController.EVENT_END_OF_FORM;
import static org.javarosa.form.api.FormEntryController.EVENT_GROUP;
import static org.javarosa.form.api.FormEntryController.EVENT_PROMPT_NEW_REPEAT;
import static org.javarosa.form.api.FormEntryController.EVENT_QUESTION;
import static org.javarosa.form.api.FormEntryController.EVENT_REPEAT;
import static org.javarosa.form.api.FormEntryController.EVENT_REPEAT_JUNCTURE;
import static org.javarosa.test.utils.ResourcePathHelper.r;
import static org.javarosa.xpath.expr.XPathPathExpr.INIT_CONTEXT_RELATIVE;
import static org.javarosa.xpath.expr.XPathStep.AXIS_ATTRIBUTE;

/**
 * <div style="border: 1px 1px 1px 1px; background-color: #556B2F; color: white; padding: 20px">
 * <b>Warning</b> This class is probably incomplete. If your testing requirements
 * aren't met by this class, please, ask around and let's try to make this tool
 * awesome together.
 * <ul>
 * <li><a href="https://slack.getodk.org">Developer Slack</a></li>
 * <li><a href="https://github.com/getodk/javarosa/issues">GitHub issues</a></li>
 * <li><a href="https://forum.getodk.org/c/development">Developer forum</a></li>
 * </ul>
 * <hr/>
 * </div>
 * <p>
 * This class helps writing JavaRosa tests. It provides two separate APIs:
 * <ul>
 * <li>A static, declarative API that lets the test author define the state
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
    public static final FormIndex BEGINNING_OF_FORM = FormIndex.createBeginningOfFormIndex();
    private Path formFile;
    private final FormDef formDef;
    private FormEntryController controller;
    private EvaluationContext evaluationContext;
    private FormEntryModel model;
    private final FormInstance blankInstance;

    private Scenario(FormDef formDef, FormEntryController controller, FormEntryModel model, EvaluationContext evaluationContext, FormInstance blankInstance) {
        this.formDef = formDef;
        this.controller = controller;
        this.evaluationContext = evaluationContext;
        this.model = model;
        this.blankInstance = blankInstance;
    }

    private static Scenario from(FormDef formDef) {
        FormEntryModel formEntryModel = new FormEntryModel(formDef);
        return new Scenario(formDef, new FormEntryController(formEntryModel), formEntryModel, formDef.getEvaluationContext(), formDef.getMainInstance().clone());
    }

    // region Miscellaneous

    public FormDef getFormDef() {
        return formDef;
    }

    public FormIndex indexOf(String xPath) {
        return getIndexOf(expandSingle(getRef(xPath)));
    }

    public FormIndex getCurrentIndex() {
        return model.getFormIndex();
    }

    public ValidateOutcome getValidationOutcome() {
        return formDef.validate(true);
    }

    /**
     * Returns a TreeReference from the provided xpath string.
     * <p>
     * This method parses the provided xpath string using the
     * XPathParseTool and postprocesses the resulting reference
     * to account for multiplicity predicates.
     * <p>
     * JavaRosa relies on negative multiplicity values to identify special
     * nodes (such as templates for repeat groups), unbound references
     * (to refer to a nodeset), and on positive multiplicity values to
     * specify individual repeat group instances.
     * <p>
     * These multiplicities are declared as numeric predicates such as
     * <code>[2]</code>, which are translated to predicates by the XPath parser.
     * This is problematic because JavaRosa will eventually try to evaluate
     * all predicates declared in references when resolving elements of an
     * instance and nodes won't ever match the predicates used to define
     * multiplicity.
     * <p>
     * For this reason, this method will try to detect these predicates,
     * turn them into multiplicity values, and remove them from the output
     * reference.
     *
     * When using the result of this method for test assertions against {@link FormIndex#getReference()} we need to
     * specify multiplicity on all steps. For example the following would pass for a form index pointing at a
     * question at the top level of the form:
     *
     * <code>
     * assertThat(formIndex.getReference(), is(getRef("/data/question[0]")));
     * </code>
     *
     * This is because <code>getRef</code> has no way of knowing if a node without multiplicity (<code>[x]</code>) is
     * a question/group or an unbounded repeat (which would have a multiplicity of <code>-1</code>). Adding the
     * explicit <code>[0]</code> lets <code>getRef</code> know that the node is not an unbounded repeat and that it,
     * like a real question or group, should have the default multiplicity of <code>0</code>.
     */
    public static TreeReference getRef(String xpath) {
        if (xpath.trim().isEmpty())
            return new TreeReference();
        try {
            TreeReference reference = ((XPathPathExpr) XPathParseTool.parseXPath(xpath)).getReference();
            for (int i = 0; i < reference.size(); i++) {
                Optional<Integer> multiplicity = extractMultiplicityFromPredicate(reference, i);
                if (multiplicity.isPresent()) {
                    reference.setMultiplicity(i, multiplicity.get());
                    reference = reference.removePredicates(i);
                }
            }
            return reference;
        } catch (XPathSyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Prepares the form to answer a new blank instance
     */
    public void newInstance() {
        formDef.setInstance(blankInstance.clone());
        formDef.initialize(true, new InstanceInitializationFactory());
        evaluationContext = formDef.getEvaluationContext();
        model = new FormEntryModel(formDef);
        controller = new FormEntryController(model);
    }

    /**
     * Sets the language of the form for itext translations
     */
    public void setLanguage(String language) {
        controller.setLanguage(language);
    }

    public EvaluationContext getEvaluationContext() {
        return evaluationContext;
    }

    /**
     * Sets a callback that will be called every time there's a DAG event
     */
    public Scenario onDagEvent(Consumer<Event> callback) {
        formDef.setEventNotifier(callback::accept);
        return this;
    }

    /**
     * Returns a new Scenario instance using a new form obtained by
     * serializing and deserializing the form being used by this instance.
     */
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
        deserializedFormDef.initialize(false, new InstanceInitializationFactory());
        return Scenario.from(deserializedFormDef);
    }

    // The fact that we need to pass in the same raw form definition that the current scenario is built around suggests
    // that XFormParser.loadXmlInstance(FormDef f, Reader xmlReader) should probably be public. This is also the method that Collect
    // copies because the FormDef may be built from cache meaning there won't be a Reader/Document available and because it makes
    // some extra calls for search(). We pass in an XFormsElement for now until we decide on an interface that Collect can use.
    public Scenario serializeAndDeserializeInstance(XFormsElement form) throws IOException, XFormParser.ParseException {
        FormInstance originalInstance = getFormDef().getMainInstance();
        XFormSerializingVisitor serializer = new XFormSerializingVisitor();
        byte[] formInstanceBytes = serializer.serializeInstance(originalInstance);

        InputStreamReader instanceReader = new InputStreamReader(new ByteArrayInputStream(formInstanceBytes));
        InputStreamReader formReader = new InputStreamReader(new ByteArrayInputStream(form.asXml().getBytes()));

        XFormParser parser = new XFormParser(formReader, instanceReader);
        FormDef restoredFormDef = parser.parse();

        Scenario restored = Scenario.from(restoredFormDef);

        return restored;
    }

    /**
     * Returns the single expanded reference of the provided reference.
     * <p>
     * This method assumes the provided reference will only be expanded
     * to exactly one reference, which is useful to go from unbound
     * references to fully qualified references that wouldn't match existing
     * form indexes otherwise.
     */
    public TreeReference expandSingle(TreeReference reference) {
        List<TreeReference> expandedRefs = evaluationContext.expandReference(reference);
        if (expandedRefs.size() != 1)
            throw new RuntimeException("Provided xPath expands to " + expandedRefs.size() + " references. Expecting exactly one expanded reference.");
        return expandedRefs.get(0);
    }

    public void trace(String msg) {
        log.info("===============================================================================");
        log.info("       " + msg);
        log.info("===============================================================================");
    }

    public void finalizeInstance() {
        controller.finalizeFormEntry();
    }

    public FormEntryController getFormEntryController() {
        return controller;
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

    private boolean refExists(TreeReference reference) {
        return evaluationContext.expandReference(reference).size() == 1;
    }

    /**
     * Returns the multiplicity value at the provided step number of the
     * provided reference if defined.
     * <p>
     * Handles special multiplicity textual representations such as <code>[@template]</code>
     */
    private static Optional<Integer> extractMultiplicityFromPredicate(TreeReference reference, int stepNumber) {
        List<XPathExpression> predicates = reference.getPredicate(stepNumber);
        if (predicates == null || predicates.size() != 1)
            return Optional.empty();

        if (isPositiveNumberPredicate(predicates))
            return Optional.ofNullable(predicates.get(0))
                .map(p -> ((XPathNumericLiteral) p).d)
                .map(Double::intValue);

        if (isNegativeNumberPredicate(predicates))
            return Optional.ofNullable(predicates.get(0))
                .map(p -> ((XPathNumNegExpr) p).a)
                .map(p -> ((XPathNumericLiteral) p).d)
                .map(Double::intValue)
                .map(i -> i * -1);

        if (isAtTemplatePredicate(predicates))
            return Optional.of(INDEX_TEMPLATE);

        return Optional.empty();
    }

    /**
     * Detects [0] (example) textual representation of a node's multiplicity as a predicate
     */
    private static boolean isPositiveNumberPredicate(List<XPathExpression> predicates) {
        return predicates.get(0) instanceof XPathNumericLiteral;
    }

    /**
     * Detects [-2] (example) textual representation of a node's multiplicity as a predicate
     */
    private static boolean isNegativeNumberPredicate(List<XPathExpression> predicates) {
        return predicates.get(0) instanceof XPathNumNegExpr && ((XPathNumNegExpr) predicates.get(0)).a instanceof XPathNumericLiteral;
    }

    /**
     * Detects the special case of [@template] textual representation of
     * a node's template multiplicity as a predicate
     */
    private static boolean isAtTemplatePredicate(List<XPathExpression> predicates) {
        return predicates.get(0) instanceof XPathPathExpr
            && ((XPathPathExpr) predicates.get(0)).steps.length == 1
            && ((XPathPathExpr) predicates.get(0)).init_context == INIT_CONTEXT_RELATIVE
            && ((XPathPathExpr) predicates.get(0)).steps[0].axis == AXIS_ATTRIBUTE
            && ((XPathPathExpr) predicates.get(0)).steps[0].name.name.equals("template");
    }

    private FormIndex getIndexOf(TreeReference ref) {
        TreeReference qualifiedRef = expandSingle(ref);
        FormIndex backupIndex = model.getFormIndex();
        silentJump(BEGINNING_OF_FORM);
        FormIndex index = model.getFormIndex();
        do {
            TreeReference refAtIndex = index.getReference();
            if (refAtIndex != null && refAtIndex.equals(qualifiedRef)) {
                silentJump(backupIndex);
                return index;
            }
            index = model.incrementIndex(index);
        } while (index.isInForm());
        silentJump(backupIndex);
        return null;
    }

    // endregion

    // region Initialization of a Scenario

    /**
     * Initializes the Scenario using a form defined using the DSL in XFormsElement
     */
    // TODO Extract the form's name from the provided XFormsElement object to simplify args
    public static Scenario init(String formName, XFormsElement form) throws IOException, XFormParser.ParseException {
        Path formFile = createTempDirectory("javarosa").resolve(formName + ".xml");
        String xml = form.asXml();
        System.out.println(xml);
        write(formFile, xml.getBytes(UTF_8), CREATE);
        return Scenario.init(formFile);
    }

    /**
     * Initializes the Scenario with provided form filename.
     * <p>
     * A form with the provided filename must exist in the classpath
     */
    public static Scenario init(String formFileName) throws XFormParser.ParseException {
        return init(r(formFileName));
    }

    /**
     * Initializes the Scenario with the form at the provided path
     */
    public static Scenario init(Path formFile) throws XFormParser.ParseException {
        // TODO explain why this sequence of calls
        StorageManager.setStorageFactory((name, type) -> new DummyIndexedStorageUtility<>());
        new XFormsModule().registerModule();
        FormParseInit fpi = new FormParseInit(formFile);
        FormDef formDef = fpi.getFormDef();
        formDef.initialize(true, new InstanceInitializationFactory());
        return Scenario.from(formDef);
    }

    // endregion

    // region Answer a specific question
    // TODO Make more overloads of these methods to have one for each data type using the correct IAnswerData subclass

    /**
     * Answers with a string value the question at the form index
     * corresponding to the provided reference.
     * <p>
     * This method has side effects:
     * - It will create all the required middle and end repeat group instances
     * - It changes the current form index
     */
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
     * This method has side effects:
     * - It will create all the required middle and end repeat group instances
     * - It changes the current form index
     */
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
     * This method has side effects:
     * - It will create all the required middle and end repeat group instances
     * - It changes the current form index
     */
    public AnswerResult answer(String xPath, int value) {
        createMissingRepeats(xPath);
        TreeReference ref = getRef(xPath);
        silentJump(getIndexOf(ref));
        return answer(value);
    }

    /**
     * Answers with a boolean value the question at the form index
     * corresponding to the provided reference.
     * <p>
     * This method has side effects:
     * - It will create all the required middle and end repeat group instances
     * - It changes the current form index
     */
    public AnswerResult answer(String xPath, boolean value) {
        createMissingRepeats(xPath);
        TreeReference ref = getRef(xPath);
        silentJump(getIndexOf(ref));
        return answer(value);
    }

    // endregion

    // region Answer the question at the form index
    // TODO Make more overloads of these methods to have one for each data type using the correct IAnswerData subclass

    /**
     * Answers the question at the form index
     */
    public AnswerResult answer(String value) {
        return answer(new StringData(value));
    }

    /**
     * Answers the question at the form index
     */
    public AnswerResult answer(List<String> values) {
        return answer(new MultipleItemsData(values.stream().map(Selection::new).collect(Collectors.toList())));
    }

    public AnswerResult answer(SelectChoice choice) {
        return answer(new SelectOneData(choice.selection()));
    }

    /**
     * Answers the question at the form index
     */
    public AnswerResult answer(int value) {
        return answer(new IntegerData(value));
    }

    /**
     * Answers the question at the form index
     */
    public AnswerResult answer(char value) {
        return answer(new StringData(String.valueOf(value)));
    }

    /**
     * Answers the question at the form index
     */
    public AnswerResult answer(LocalDate value) {
        return answer(new DateData(Date.valueOf(value)));
    }

    /**
     * Answers the question at the form index
     */
    public AnswerResult answer(boolean value) {
        return answer(new BooleanData(value));
    }

    private AnswerResult answer(IAnswerData data) {
        FormIndex formIndex = model.getFormIndex();
        log.info("Answer {} at {}", data, formIndex.getReference().toString(true, true));
        return AnswerResult.from(controller.answerQuestion(formIndex, data, true));
    }

    // endregion

    // region Repeat group manipulation

    /**
     * Removes the repeat instance corresponding to the provided reference
     */
    public Scenario removeRepeat(String xPath) {
        TreeReference reference = expandSingle(getRef(xPath));

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

    /**
     * Creates a new repeat group instance. The form index must be
     * at a create new repeat group question
     */
    public Scenario createNewRepeat() {
        log.info("Create repeat instance {}", model.getFormIndex().getReference());
        controller.newRepeat();
        return this;
    }

    /**
     * Creates a new repeat group instance in the group corresponding
     * to the provided xPath reference
     */
    public Scenario createNewRepeat(String xPath) {
        TreeReference groupRef = getRef(xPath);
        if (!groupRef.isAmbiguous())
            throw new RuntimeException("Provided xPath must be ambiguous");

        // Compute the next multiplicity value counting the existing instances
        TreeReference repeatInstanceRef = groupRef.clone();
        int multiplicity = evaluationContext.expandReference(repeatInstanceRef).size();
        repeatInstanceRef.setMultiplicity(repeatInstanceRef.size() - 1, multiplicity);

        return createRepeat(repeatInstanceRef);
    }

    /**
     * Creates a repeat group corresponding to the specific repeat
     * instance reference, creating middle instances if necessary
     * to reach the specified multiplicity.
     */
    private Scenario createRepeat(TreeReference repeatInstanceRef) {
        if (repeatInstanceRef.isAmbiguous())
            throw new RuntimeException("The provided reference can't be ambiguous");

        silentJump(BEGINNING_OF_FORM);
        while (!atTheEndOfForm() && !refExists(repeatInstanceRef))
            if (silentNext() == EVENT_PROMPT_NEW_REPEAT) {
                if (model.getFormIndex().getReference().equals(repeatInstanceRef))
                    // We're in one (probably the first) of the siblings of the
                    // repeat group at the subRef. Create new repeats until the
                    // one want we need is created
                    while (!refExists(repeatInstanceRef))
                        controller.descendIntoNewRepeat();
            }
        if (!refExists(repeatInstanceRef))
            throw new RuntimeException("We couldn't create repeat group instance at " + repeatInstanceRef + ". Check your form and your test");
        return this;
    }

    private void createMissingRepeats(String xPath) {
        FormIndex backupIndex = model.getFormIndex();
        TreeReference reference = getRef(xPath);
        for (int i = 0; i < reference.size(); i++) {
            if (reference.getMultiplicity(i) < 0)
                // This part has no multiplicity, so we don't need to do anything.
                continue;
            createRepeat(reference.getSubReference(i));
        }
        silentJump(backupIndex);
    }

    // endregion

    // region Traversing the form

    /**
     * Jump to the next event.
     * <p>
     * Side effects:
     * - This method updates the form index
     * - This method leaves log traces
     */
    public int next() {
        int jumpResultCode = controller.stepToNextEvent();
        log.info(humanJumpTrace(jumpResultCode));
        return jumpResultCode;
    }

    public int prev() {
        int jumpResultCode = controller.stepToPreviousEvent();
        log.info(humanJumpTrace(jumpResultCode));
        return jumpResultCode;
    }

    /**
     * Jump the provided amount of times to the next event.
     * <p>
     * Side effects:
     * - This method updates the form index
     * - This method leaves log traces
     */
    public void next(int amount) {
        while (amount-- > 0)
            next();
    }

    /**
     * Jump to the beginning of the form.
     * <p>
     * Side effects:
     * - This method updates the form index
     * - This method leaves log traces
     */
    public void jumpToBeginningOfForm() {
        jump(BEGINNING_OF_FORM);
    }

    private int silentNext() {
        return controller.stepToNextEvent();
    }

    private int silentPrev() {
        return controller.stepToPreviousEvent();
    }

    private int jump(FormIndex index) {
        int jumpResultCode = controller.jumpToIndex(index);
        log.info(humanJumpTrace(jumpResultCode));
        return jumpResultCode;
    }

    private void silentJump(FormIndex indexOf) {
        controller.jumpToIndex(indexOf);
    }

    private String humanJumpTrace(int jumpResultCode) {
        FormIndex formIndex = model.getFormIndex();
        String humanJumpResult = decodeJumpResult(jumpResultCode);
        IFormElement element = formDef.getChild(formIndex);
        String humanLabel = Optional.ofNullable(element.getLabelInnerText()).orElseGet(() -> {
            Localizer localizer = formDef.getLocalizer();
            String textId = element.getTextID();

            if (textId == null || localizer == null) {
                return "";
            }

            return Optional.ofNullable(localizer.getText(textId))
                .map(this::trimToOneLine)
                .orElse("");
        });
        String humanReference = Optional.ofNullable(formIndex.getReference())
            .map(ref -> ref.toString(true, true))
            .orElse("");

        return String.format(
            "Jump to %s%s%s",
            humanJumpResult,
            prefixIfNotEmpty(" ", humanLabel),
            prefixIfNotEmpty(" ref:", humanReference));
    }

    private String prefixIfNotEmpty(String prefix, String text) {
        return text.isEmpty() ? "" : prefix + text;
    }

    private String trimToOneLine(String text) {
        return Stream.of(text.split("\n"))
            .map(String::trim)
            .collect(joining(" "));
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
        return model.getFormIndex().isEndOfFormIndex();
    }

    public TreeReference nextRef() {
        silentNext();
        TreeReference ref = refAtIndex();
        silentPrev();
        return ref;
    }

    public TreeReference refAtIndex() {
        return controller.getModel().getFormIndex().getReference();
    }

    public boolean atQuestion() {
        return formDef.getChild(controller.getModel().getFormIndex()) instanceof QuestionDef;
    }

    public QuestionDef getQuestionAtIndex() {
        return model.getQuestionPrompt().getQuestion();
    }

    public FormEntryPrompt getFormEntryPromptAtIndex() {
        return model.getQuestionPrompt();
    }

    // endregion

    // region Inspect the main instance

    @SuppressWarnings("unchecked")
    public <T extends IAnswerData> T answerOf(String xPath) {
        TreeReference reference = getRef(xPath);
        if (!refExists(reference))
            return null;

        TreeElement element = formDef.getMainInstance().resolveReference(reference);
        return element != null ? (T) element.getValue() : null;
    }

    public int countRepeatInstancesOf(String xPath) {
        TreeReference reference = getRef(xPath);
        if (!reference.isAmbiguous())
            throw new RuntimeException("Provided xPath must be ambiguous");

        List<TreeReference> treeReferences = evaluationContext
            .expandReference(reference);
        return treeReferences
            .size();
    }

    /**
     * Returns the list of choices of the &lt;select&gt; or &lt;select1&gt; form controls.
     * <p>
     * This method ensures that any dynamic choice lists are populated to reflect the status
     * of the form (already answered questions, etc.).
     */
    public List<SelectChoice> choicesOf(String xPath) {
        TreeReference reference = expandSingle(getRef(xPath));

        FormEntryPrompt questionPrompt = model.getQuestionPrompt(getIndexOf(reference));
        // This call triggers the correct population of dynamic choices.
        questionPrompt.getAnswerValue();
        QuestionDef control = questionPrompt.getQuestion();
        return control.getChoices() == null
            // If the (static) choices is null, that means there is an itemset and choices are dynamic
            // ItemsetBinding.getChoices() will work because we've called questionPrompt.getAnswerValue()
            ? control.getDynamicChoices().getChoices(formDef, reference)
            : control.getChoices();
    }

    public TreeElement getAnswerNode(String xPath) {
        return formDef.getMainInstance().resolveReference(expandSingle(getRef(xPath)));
    }

    // endregion
}
