package org.javarosa;

import static org.javarosa.core.model.instance.TreeReference.CONTEXT_ABSOLUTE;
import static org.javarosa.core.model.instance.TreeReference.INDEX_UNBOUND;
import static org.javarosa.core.model.instance.TreeReference.REF_ABSOLUTE;

import java.util.Arrays;
import java.util.List;
import org.javarosa.core.model.FormDef;
import org.javarosa.core.model.FormIndex;
import org.javarosa.core.model.SelectChoice;
import org.javarosa.core.model.instance.InstanceInitializationFactory;
import org.javarosa.core.model.instance.TreeReference;
import org.javarosa.form.api.FormEntryPrompt;

public class TestHelper {
    /**
     * Builds an absolute {@link TreeReference} from a string path description
     * like <code>/some/path</code>.
     * <p>
     * Can be used in combination of other methods that take {@link TreeReference},
     * like {@link #getFormIndex(FormDef, TreeReference)} or {@link #getSelectChoices(FormDef, String)}
     * to make them easier to use. Example:
     *
     * <code>FormIndex formIndex = getFormIndex(formDef, absoluteRef("/data/field"));</code>
     */
    public static TreeReference absoluteRef(String path) {
        TreeReference tr = new TreeReference();
        tr.setRefLevel(REF_ABSOLUTE);
        tr.setContext(CONTEXT_ABSOLUTE);
        tr.setInstanceName(null);
        Arrays.stream(path.split("/"))
            .filter(s -> !s.isEmpty())
            .forEach(s -> tr.add(s, INDEX_UNBOUND));
        return tr;
    }

    /**
     * Returns a {@link FormIndex} corresponding to the given string reference.
     * <p>
     * The absolute {@link TreeReference} of the question is built with {@link #absoluteRef(String)}.
     */
    public static FormIndex getFormIndex(FormDef formDef, String ref) {
        return getFormIndex(formDef, absoluteRef(ref));
    }

    /**
     * Returns a {@link FormIndex} corresponding to the given {@link TreeReference}.
     */
    public static FormIndex getFormIndex(FormDef formDef, TreeReference ref) {
        for (int localIndex = 0, lastIndex = formDef.getChildren().size(); localIndex < lastIndex; localIndex++)
            if (formDef.getChild(localIndex).getBind().getReference().equals(ref))
                return new FormIndex(localIndex, 0, ref);
        throw new IllegalArgumentException("Reference " + ref + " not found");
    }

    /**
     * Returns a {@link FormEntryPrompt} corresponding to the given string reference.
     * <p>
     * The absolute {@link TreeReference} of the question is built with {@link #absoluteRef(String)}.
     */
    public static FormEntryPrompt getFormEntryPrompt(FormDef formDef, String ref) {
        return new FormEntryPrompt(formDef, getFormIndex(formDef, ref));
    }

    /**
     * Returns a {@link List} of {@link SelectChoice} elements of a question at the
     * given string reference.
     * <p>
     * The absolute {@link TreeReference} of the question is built with {@link #absoluteRef(String)}.
     */
    public static List<SelectChoice> getSelectChoices(FormDef formDef, String ref) {
        FormIndex formIndex = getFormIndex(formDef, absoluteRef(ref));
        FormEntryPrompt formEntryPrompt = new FormEntryPrompt(formDef, formIndex);
        return formEntryPrompt.getSelectChoices();
    }

    /**
     * Returns the value of an answer at the given string reference.
     */
    public static Object getAnswerValue(FormDef formDef, String ref) {
        FormIndex formIndex = getFormIndex(formDef, absoluteRef(ref));
        FormEntryPrompt formEntryPrompt = new FormEntryPrompt(formDef, formIndex);
        return formEntryPrompt.getAnswerValue().getValue();
    }

    /**
     * Initializes a new instance in the given {@link FormDef} form.
     */
    public static void initializeNewInstance(FormDef formDef) {
        formDef.initialize(true, new InstanceInitializationFactory());
    }
}
