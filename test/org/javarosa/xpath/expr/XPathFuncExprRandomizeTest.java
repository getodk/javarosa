/*
 * Copyright 2018 Nafundi
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
package org.javarosa.xpath.expr;

import static org.javarosa.core.model.instance.TreeReference.CONTEXT_ABSOLUTE;
import static org.javarosa.core.model.instance.TreeReference.INDEX_UNBOUND;
import static org.javarosa.core.model.instance.TreeReference.REF_ABSOLUTE;
import static org.javarosa.test.utils.ResourcePathHelper.r;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.List;
import org.javarosa.core.model.FormDef;
import org.javarosa.core.model.FormIndex;
import org.javarosa.core.model.SelectChoice;
import org.javarosa.core.model.instance.InstanceInitializationFactory;
import org.javarosa.core.model.instance.TreeReference;
import org.javarosa.core.test.FormParseInit;
import org.javarosa.form.api.FormEntryPrompt;
import org.junit.Before;
import org.junit.Test;

public class XPathFuncExprRandomizeTest {

    private FormDef formDef;

    @Before
    public void setUp() {
        // Take a look to the form file:
        //  - The first pair of fields use randomize without a seed.
        //  - The second pair of fields use randomize with a seed.
        FormParseInit fpi = new FormParseInit();
        fpi.setFormToParse(r("randomize.xml").toString());
        formDef = fpi.getFormDef();
    }

    @Test
    public void fields_without_seed_in_the_same_form_get_a_different_order_of_choices() {
        initializeNewInstance(formDef);
        List<SelectChoice> choices1 = getSelectChoices(formDef, "/randomize/fruit1");
        List<SelectChoice> choices2 = getSelectChoices(formDef, "/randomize/fruit2");

        assertFalse(nodesEqualInOrder(choices1, choices2));
    }

    @Test
    public void the_same_field_without_seed_in_different_instances_gets_a_different_order_of_choices() {
        initializeNewInstance(formDef);
        List<SelectChoice> choices1 = getSelectChoices(formDef, "/randomize/fruit1");

        initializeNewInstance(formDef);
        List<SelectChoice> choices2 = getSelectChoices(formDef, "/randomize/fruit1");

        assertFalse(nodesEqualInOrder(choices1, choices2));
    }

    @Test
    public void seeded_fields_in_the_same_form_get_the_same_order_of_choices() {
        initializeNewInstance(formDef);
        List<SelectChoice> choices1 = getSelectChoices(formDef, "/randomize/seededFruit1");
        List<SelectChoice> choices2 = getSelectChoices(formDef, "/randomize/seededFruit2");

        assertTrue(nodesEqualInOrder(choices1, choices2));
    }

    @Test
    public void the_same_seeded_field_in_different_instances_gets_the_same_order_of_choices() {
        initializeNewInstance(formDef);
        List<SelectChoice> choices1 = getSelectChoices(formDef, "/randomize/seededFruit2");

        initializeNewInstance(formDef);
        List<SelectChoice> choices2 = getSelectChoices(formDef, "/randomize/seededFruit2");

        assertTrue(nodesEqualInOrder(choices1, choices2));
    }

    private static void initializeNewInstance(FormDef formDef) {
        formDef.initialize(true, new InstanceInitializationFactory());
    }

    private List<SelectChoice> getSelectChoices(FormDef formDef, String ref) {
        FormIndex formIndex = getFormIndex(formDef, absoluteRef(ref));
        FormEntryPrompt formEntryPrompt = new FormEntryPrompt(formDef, formIndex);
        return formEntryPrompt.getSelectChoices();
    }

    private FormIndex getFormIndex(FormDef formDef, TreeReference ref) {
        for (int localIndex = 0, lastIndex = formDef.getChildren().size(); localIndex < lastIndex; localIndex++)
            if (formDef.getChild(localIndex).getBind().getReference().equals(ref))
                return new FormIndex(localIndex, 0, ref);
        throw new IllegalArgumentException("Reference " + ref + " not found");
    }

    private static boolean nodesEqualInOrder(List<SelectChoice> left, List<SelectChoice> right) {
        if (left.size() != right.size())
            return false;

        for (int i = 0; i < left.size(); i++)
            if (!left.get(i).getValue().equals(right.get(i).getValue()))
                return false;

        return true;
    }

    private static TreeReference absoluteRef(String path) {
        TreeReference tr = new TreeReference();
        tr.setRefLevel(REF_ABSOLUTE);
        tr.setContext(CONTEXT_ABSOLUTE);
        tr.setInstanceName(null);
        Arrays.stream(path.split("/"))
            .filter(s -> !s.isEmpty())
            .forEach(s -> tr.add(s, INDEX_UNBOUND));
        return tr;
    }
}