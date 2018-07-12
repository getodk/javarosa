package org.javarosa.xpath.expr;

import static org.javarosa.core.model.instance.TreeReference.CONTEXT_ABSOLUTE;
import static org.javarosa.core.model.instance.TreeReference.REF_ABSOLUTE;
import static org.javarosa.test.utils.ResourcePathHelper.r;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import org.javarosa.core.model.FormDef;
import org.javarosa.core.model.FormIndex;
import org.javarosa.core.model.QuestionDef;
import org.javarosa.core.model.SelectChoice;
import org.javarosa.core.model.data.IAnswerData;
import org.javarosa.core.model.data.StringData;
import org.javarosa.core.model.instance.InstanceInitializationFactory;
import org.javarosa.core.model.instance.TreeElement;
import org.javarosa.core.model.instance.TreeReference;
import org.javarosa.core.test.FormParseInit;
import org.javarosa.form.api.FormEntryController;
import org.javarosa.form.api.FormEntryModel;
import org.javarosa.form.api.FormEntryPrompt;

class Scenario {
    private final FormDef formDef;
    private final FormEntryController formEntryController;

    private Scenario(FormDef formDef, FormEntryController formEntryController) {
        this.formDef = formDef;
        this.formEntryController = formEntryController;
    }

    /**
     * Creates and prepares the test scenario loading and parsing the given form
     */
    public static Scenario init(String formFileName) {
        // TODO explain why this sequence of calls
        FormParseInit fpi = new FormParseInit(r(formFileName));
        FormDef formDef = fpi.getFormDef();
        formDef.initialize(true, new InstanceInitializationFactory());
        FormEntryModel formEntryModel = new FormEntryModel(formDef);
        FormEntryController formEntryController = new FormEntryController(formEntryModel);
        return new Scenario(formDef, formEntryController);
    }

    /**
     * Sets the value of the element located at the given xPath in the main instance.
     * <p>
     * This method supports an enhanced version of xpath with the following perks and
     * limitations:
     * <ul>
     * <li>Only supports absolute xpaths</li>
     * <li>Supports adding the index (zero-indexed) of a repeat instance by suffixing it between
     * brackets. Example that would select the fourth instance of the <code>/foo/bar</code>
     * repeat: <code>/foo/bar[3]</code></li>
     * </ul>
     * <p>
     * This method ensures that all the repeat instances required by the given xPath
     * exist. For example: /data/people[1]/name will make sure the second repeat for
     * /data/people exists.
     * <p>
     * This method ensures that any required downstream change after the given value
     * is set is triggered.
     */
    // TODO Make more of these, one for each data type, and use the correct IAnswerData type
    public void answer(String xPath, String value) {
        // the xPath could include repeat group indexes, meaning that we expect
        // some repeat group to exists. We check that and create missing repeats
        // where they're needed.
        createMissingRepeats(xPath);

        // The xPath must match a model element. This ensures we can resolve it.
        TreeElement element = Objects.requireNonNull(resolve(xPath));

        // We wrap the given value and set it through the formDef, which triggers
        // any downstream change in dependant elements (elements that have a
        // calculate formula depending on this field, or itemsets rendering this
        // field's value as a choice)
        formDef.setValue(new StringData(value), element.getRef(), true);
    }

    /**
     * Returns the value of the element located at the given xPath in the main instance.
     * <p>
     * This method supports an enhanced version of xpath with the following perks and
     * limitations:
     * <ul>
     * <li>Only supports absolute xpaths</li>
     * <li>Supports adding the index (zero-indexed) of a repeat instance by suffixing it between
     * brackets. Example that would select the fourth instance of the <code>/foo/bar</code>
     * repeat: <code>/foo/bar[3]</code></li>
     * </ul>
     * <p>
     * Answers live in the main instance of a form. We will traverse the main
     * instance's tree of elements recursively using the xPath as a guide of
     * steps.
     * <p>
     * The starting point will be the NULL node parent of main instance's root,
     * which corresponds to the root "/" xPath.
     * <p>
     * Note that the formDef.getMainInstance().getRoot() call can be misleading
     * because it would return an element corresponding to the xPath "/data"
     * ("data" is commonly used as the main instance's xml tag), not the root
     * element.
     */
    @SuppressWarnings("unchecked")
    public <T extends IAnswerData> T answerOf(String xPath) {
        // Get the real root element
        TreeElement root = (TreeElement) formDef.getMainInstance().getRoot().getParent();
        // Since we start searching from "/", we make the input xPath relative to that
        String relativeXPath = xPath.startsWith("/") ? xPath.substring(1) : xPath;

        // We call the recursive resolve algorithm and get the element
        TreeElement element = resolve(root, relativeXPath);

        // Return the value if the element exists, otherwise return null
        return element != null ? (T) element.getValue() : null;
    }

    /**
     * Returns the list of choices of the &lt;select&gt; or &lt;select1&gt; form controls.
     * <p>
     * This method supports an enhanced version of xpath with the following perks and
     * limitations:
     * <ul>
     * <li>Only supports absolute xpaths</li>
     * <li>Supports adding the index (zero-indexed) of a repeat instance by suffixing it between
     * brackets. Example that would select the fourth instance of the <code>/foo/bar</code>
     * repeat: <code>/foo/bar[3]</code></li>
     * </ul>
     * <p>
     * This method ensures that any dynamic choce lists are populated to reflect the status
     * of the form (already answered questions, etc.).
     */
    public List<SelectChoice> choicesOf(String xPath) {
        FormEntryPrompt questionPrompt = formEntryController.getModel().getQuestionPrompt(getIndexOf(xPath));
        // This call triggers the correct population of dynamic choices.
        questionPrompt.getAnswerValue();
        QuestionDef control = questionPrompt.getQuestion();
        return control.getChoices() == null
            // If the (static) choices is null, that means there is an itemset and choices are dynamic
            // ItemsetBinding.getChoices() will work because we've called questionPrompt.getAnswerValue()
            ? control.getDynamicChoices().getChoices()
            : control.getChoices();
    }

    /**
     * Prepare a new (main) instance in the form, to simulate the user starting to fill a
     * new form.
     */
    public void newInstance() {
        formDef.initialize(true, new InstanceInitializationFactory());
    }

    private void createMissingRepeats(String xPath) {
        // We will be looking to the parts in the xPath from left to right.
        // xPath.substring(1) makes the first "/" char go away, giving us an xPath relative to the root
        List<String> parts = Arrays.asList(xPath.substring(1).split("/"));
        String currentXPath = "";
        do {
            String nextPart = parts.get(0);
            // nextName holds the next part's name, excluding the multiplicity suffix if it exists.
            String nextName = parseName(nextPart);
            String nextXPath = currentXPath + "/" + nextPart;

            if (isRepeatXPath(nextXPath) && !elementExists(nextXPath)) {
                // Jumping to the repeat instance for [0] always works because formEntryController.descendIntoNewRepeat() deals with it:
                // - if the [0] doesn't exists, it creates it
                // - if the [0] exists, looks for the next sequential repeat and creates it
                formEntryController.jumpToIndex(getIndexOf((currentXPath + "/" + nextName) + "[0]"));
                formEntryController.descendIntoNewRepeat();
            }
            // Shift the first part of the list, reset the current xPath and loop
            parts = parts.subList(1, parts.size());
            currentXPath = nextXPath;
        } while (!parts.isEmpty());
    }

    private boolean elementExists(String xPath) {
        return resolve(xPath) != null;
    }

    private static boolean isRepeatXPath(String xPath) {
        // Returns true if the last xPath part contains a multiplicity suffix like "[2]"
        return xPath.substring(xPath.lastIndexOf("/")).contains("[");
    }

    /**
     * Returns an absolute reference of the given xPath taking multiplicity of each
     * xPath part into account.
     */
    private TreeReference absoluteRef(String xPath) {
        TreeReference tr = new TreeReference();
        tr.setRefLevel(REF_ABSOLUTE);
        tr.setContext(CONTEXT_ABSOLUTE);
        tr.setInstanceName(null);
        Arrays.stream(xPath.split("/"))
            .filter(s -> !s.isEmpty())
            .forEach(s -> tr.add(parseName(s), parseMultiplicity(s)));
        return tr;
    }

    private String parseName(String xPathPart) {
        return xPathPart.contains("[") ? xPathPart.substring(0, xPathPart.indexOf("[")) : xPathPart;
    }

    private int parseMultiplicity(String xPathPart) {
        return xPathPart.contains("[") ? Integer.parseInt(xPathPart.substring(xPathPart.indexOf("[") + 1, xPathPart.indexOf("]"))) : 0;
    }

    /**
     * Returns an index to the given xPath. It uses an absolute reference to
     * the given xPath and traverses all existing indexes until one of them
     * matches the reference.
     * <p>
     * This is possible because the {@link TreeReference#equals(Object)} can
     * deal with absolute and relative references.
     * <p>
     * Returns null if the reference is not found.
     */
    private FormIndex getIndexOf(String xPath) {
        TreeReference ref = absoluteRef(xPath);
        formEntryController.jumpToIndex(FormIndex.createBeginningOfFormIndex());
        FormEntryModel model = formEntryController.getModel();
        FormIndex index = model.getFormIndex();
        do {
            if (index.getReference() != null && index.getReference().equals(ref))
                return index;
            index = model.incrementIndex(index);
        } while (index.isInForm());
        return null;
    }


    /**
     * Returns the element corresponding to the given xPath.
     * <p>
     * The starting point will be the NULL node parent of main instance's root,
     * which corresponds to the root "/" xPath.
     * <p>
     * Note that the formDef.getMainInstance().getRoot() call can be misleading
     * because it would return an element corresponding to the xPath "/data"
     * ("data" is commonly used as the main instance's xml tag), not the root
     * element.
     */
    private TreeElement resolve(String xPath) {
        // Get the real root element
        TreeElement root = (TreeElement) formDef.getMainInstance().getRoot().getParent();
        // Since we start searching from "/", we make the input xPath relative to that
        String relativeXPath = xPath.startsWith("/") ? xPath.substring(1) : xPath;

        return resolve(root, relativeXPath);
    }

    /**
     * Returns the element corresponding to the given xPath.
     * <p>
     * It does so by recursively traversing children of the given element and calling
     * {@link TreeElement#getChild(String, int)} on them.
     */
    private TreeElement resolve(TreeElement element, String xPath) {
        List<String> parts = Arrays.asList(xPath.split("/"));
        String firstPart = parts.get(0);
        TreeElement nextElement = element.getChild(parseName(firstPart), parseMultiplicity(firstPart));

        // Return null when a child with the given name and multiplicity doesn't exist.
        if (nextElement == null)
            return null;

        // If there are more parts to analyze, call recursively on child
        if (parts.size() > 1)
            return resolve(nextElement, shift(xPath));

        // If this is the last part in the xPath, we have the element we're looking for
        return nextElement;
    }

    private static String shift(String xPath) {
        List<String> parts = Arrays.asList(xPath.split("/"));
        return String.join("/", parts.subList(1, parts.size()));
    }
}
