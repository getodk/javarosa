package org.javarosa.core.model.condition;

import static org.javarosa.core.model.FormDef.findQuestionByRef;

import java.util.ArrayList;
import java.util.List;
import org.javarosa.core.log.WrappedException;
import org.javarosa.core.model.Constants;
import org.javarosa.core.model.FormDef;
import org.javarosa.core.model.ItemsetBinding;
import org.javarosa.core.model.QuestionDef;
import org.javarosa.core.model.SelectChoice;
import org.javarosa.core.model.instance.TreeReference;
import org.javarosa.core.model.util.restorable.RestoreUtils;
import org.jetbrains.annotations.NotNull;

public class ChoiceNameFunctionHandler implements IFunctionHandler {
    FormDef f;

    public ChoiceNameFunctionHandler(final FormDef f) {
        this.f = f;
    }

    @Override
    public @NotNull String getName() {
        return "jr:choice-name";
    }

    @Override
    public @NotNull Object eval(Object @NotNull [] args, @NotNull EvaluationContext ec) {
        try {
            String value = (String) args[0];
            String questionXpath = (String) args[1];
            TreeReference ref = RestoreUtils.xfFact.ref(questionXpath);
            ref = ref.anchor(ec.getContextRef());

            QuestionDef q = findQuestionByRef(ref, f);
            if (q == null
                || (q.getControlType() != Constants.CONTROL_SELECT_ONE
                && q.getControlType() != Constants.CONTROL_SELECT_MULTI
                && q.getControlType() != Constants.CONTROL_RANK)) {
                return "";
            }

            List<SelectChoice> choices;

            ItemsetBinding itemset = q.getDynamicChoices();
            if (itemset != null) {
                // 2019-HM: See ChoiceNameTest for test and more explanation

                // NOTE: We have no context against which to evaluate a dynamic selection list. This will
                // generally cause that evaluation to break if any filtering is done, or, worst case, give
                // unexpected results.
                //
                // We should hook into the existing code (FormEntryPrompt) for pulling display text for select
                // choices. however, it's hard, because we don't really have any context to work with, and all
                // the situations where that context would be used don't make sense for trying to reverse a
                // select value back to a label in an unrelated expression
                if (ref.isAmbiguous()) {
                    // ref, the reference used to specify where choices are defined, could be an absolute
                    // reference to a repeat nodeset. In that case, we need to convert that nodeset ref
                    // into a node ref. First try to contextualize based on the current repeat in case the
                    // choice-name call is from inside a repeat. Then use position 1 for any repeats that
                    // weren't contextualized (this is what a standards-compliant XPath engine would always do).
                    ref = ref.contextualize(ec.getContextRef());

                    for (int i = 0; i < ref.size(); i++) {
                        if (ref.getMultiplicity(i) == TreeReference.INDEX_UNBOUND) {
                            ref.setMultiplicity(i, TreeReference.DEFAULT_MULTIPLICITY);
                        }
                    }
                }
                choices = itemset.getChoices(f, ref);
            } else { // static choices
                choices = q.getChoices();
            }
            if (choices != null) {
                for (SelectChoice ch : choices) {
                    if (ch.getValue().equals(value)) {
                        // this is really not ideal. we should hook into the existing code (FormEntryPrompt)
                        // for pulling display text for select choices. however, it's hard, because we don't
                        // really have any context to work with, and all the situations where that context
                        // would be used don't make sense for trying to reverse a select value back to a
                        // label in an unrelated expression

                        String textID = ch.getTextID();
                        String templateStr;
                        if (textID != null) {
                            templateStr = f.getLocalizer().getText(textID);
                        } else {
                            templateStr = ch.getLabelInnerText();
                        }
                        return f.fillTemplateString(templateStr, ref);
                    }
                }
            }
            return "";
        } catch (Exception e) {
            throw new WrappedException("error in evaluation of xpath function [choice-name]",
                e);
        }
    }

    @Override
    public @NotNull List<Class<?>[]> getPrototypes() {
        Class<?>[] proto = {String.class, String.class};
        List<Class<?>[]> v = new ArrayList<>(1);
        v.add(proto);
        return v;
    }

    @Override
    public boolean rawArgs() {
        return false;
    }
}
