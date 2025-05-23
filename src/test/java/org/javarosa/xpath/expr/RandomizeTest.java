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

import org.javarosa.core.model.CoreModelModule;
import org.javarosa.core.model.FormDef;
import org.javarosa.core.model.FormIndex;
import org.javarosa.core.model.SelectChoice;
import org.javarosa.core.model.instance.InstanceInitializationFactory;
import org.javarosa.core.model.instance.TreeReference;
import org.javarosa.core.services.PrototypeManager;
import org.javarosa.core.util.JavaRosaCoreModule;
import org.javarosa.core.util.externalizable.DeserializationException;
import org.javarosa.form.api.FormEntryPrompt;
import org.javarosa.model.xform.XFormsModule;
import org.javarosa.test.FormParseInit;
import org.javarosa.xform.parse.XFormParser;
import org.junit.Before;
import org.junit.Test;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;

import static java.nio.file.Files.createTempFile;
import static java.nio.file.Files.delete;
import static java.nio.file.Files.newInputStream;
import static java.nio.file.Files.newOutputStream;
import static org.javarosa.core.model.instance.TreeReference.CONTEXT_ABSOLUTE;
import static org.javarosa.core.model.instance.TreeReference.INDEX_UNBOUND;
import static org.javarosa.core.model.instance.TreeReference.REF_ABSOLUTE;
import static org.javarosa.test.ResourcePathHelper.r;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class RandomizeTest {

    private FormDef formDef;

    @Before
    public void setUp() throws XFormParser.ParseException {
        // Take a look to the form file:
        //  - The first pair of fields use randomize without a seed.
        //  - The second pair of fields use randomize with a seed.
        FormParseInit fpi = new FormParseInit(r("randomize.xml"));
        formDef = fpi.getFormDef();
    }

    @Test
    public void fields_without_seed_in_the_same_form_get_a_different_order_of_choices() {
        initializeNewInstance(formDef);
        List<SelectChoice> choices1 = getSelectChoices(formDef, "/data/no-seed-fruit1");
        List<SelectChoice> choices2 = getSelectChoices(formDef, "/data/no-seed-fruit2");

        assertFalse(nodesEqualInOrder(choices1, choices2));
    }

    @Test
    public void the_same_field_without_seed_in_different_instances_gets_a_different_order_of_choices() {
        initializeNewInstance(formDef);
        List<SelectChoice> choices1 = getSelectChoices(formDef, "/data/no-seed-fruit1");

        initializeNewInstance(formDef);
        List<SelectChoice> choices2 = getSelectChoices(formDef, "/data/no-seed-fruit1");

        assertFalse(nodesEqualInOrder(choices1, choices2));
    }

    @Test
    public void seeded_fields_in_the_same_form_get_the_same_order_of_choices() {
        initializeNewInstance(formDef);
        List<SelectChoice> choices1 = getSelectChoices(formDef, "/data/static-seed-fruit1");
        List<SelectChoice> choices2 = getSelectChoices(formDef, "/data/static-seed-fruit2");

        assertTrue(nodesEqualInOrder(choices1, choices2));
    }

    @Test
    public void the_same_seeded_field_in_different_instances_gets_the_same_order_of_choices() {
        initializeNewInstance(formDef);
        List<SelectChoice> choices1 = getSelectChoices(formDef, "/data/static-seed-fruit2");

        initializeNewInstance(formDef);
        List<SelectChoice> choices2 = getSelectChoices(formDef, "/data/static-seed-fruit2");

        assertTrue(nodesEqualInOrder(choices1, choices2));
    }

    @Test
    public void the_same_seeded_field_in_different_instances_from_deserialized_forms_gets_the_same_order_of_choices() throws IOException, DeserializationException {
        initializeNewInstance(formDef);
        List<SelectChoice> choices1 = getSelectChoices(formDef, "/data/static-seed-fruit2");

        FormDef formDefAfterSerialization = serializeAndDeserializeForm(formDef);

        initializeNewInstance(formDefAfterSerialization);
        List<SelectChoice> choices2 = getSelectChoices(formDefAfterSerialization, "/data/static-seed-fruit2");

        assertTrue(nodesEqualInOrder(choices1, choices2));
    }

    @Test
    public void randomize_function_can_be_used_outside_itemset_nodeset_definitions() {
        initializeNewInstance(formDef);
        // The ref /data/randomValue is the max from a randomized nodeset of numbers from 1 to 6
        assertEquals(6, getAnswerValue(formDef, "/data/no-seed-random-value"));
    }

    @Test
    public void seeded_randomize_function_can_be_used_outside_itemset_nodeset_definitions() {
        initializeNewInstance(formDef);
        // The ref /data/seededRandomValue is the max from a randomized nodeset of numbers from 1 to 6
        assertEquals(6, getAnswerValue(formDef, "/data/static-seed-random-value"));
    }

    @Test
    public void randomize_function_can_take_a_seed_from_a_nodeset() {
        initializeNewInstance(formDef);
        // The ref /data/seededRandomValue is the max from a randomized nodeset of numbers from 1 to 6
        assertEquals(6, getAnswerValue(formDef, "/data/nodeset-seed-random-value"));
    }

    @Test
    public void fields_can_take_their_randomize_seeds_from_a_nodeset() throws IOException, DeserializationException {
        initializeNewInstance(formDef);
        List<SelectChoice> choices1a = getSelectChoices(formDef, "/data/nodeset-seed-fruit1");
        List<SelectChoice> choices2a = getSelectChoices(formDef, "/data/nodeset-seed-fruit2");

        initializeNewInstance(formDef);
        List<SelectChoice> choices1b = getSelectChoices(formDef, "/data/nodeset-seed-fruit1");
        List<SelectChoice> choices2b = getSelectChoices(formDef, "/data/nodeset-seed-fruit2");

        FormDef formDefAfterSerialization = serializeAndDeserializeForm(formDef);

        initializeNewInstance(formDef);
        List<SelectChoice> choices1c = getSelectChoices(formDefAfterSerialization, "/data/nodeset-seed-fruit1");
        List<SelectChoice> choices2c = getSelectChoices(formDefAfterSerialization, "/data/nodeset-seed-fruit2");

        assertTrue(nodesEqualInOrder(choices1a, choices2a));
        assertTrue(nodesEqualInOrder(choices1a, choices1b));
        assertTrue(nodesEqualInOrder(choices1a, choices1c));
        assertTrue(nodesEqualInOrder(choices2a, choices2b));
        assertTrue(nodesEqualInOrder(choices2a, choices2c));
    }

    private FormDef serializeAndDeserializeForm(FormDef formDef) throws IOException, DeserializationException {
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
        return deserializedFormDef;
    }

    private static void initializeNewInstance(FormDef formDef) {
        formDef.initialize(true, new InstanceInitializationFactory());
    }

    private List<SelectChoice> getSelectChoices(FormDef formDef, String ref) {
        FormIndex formIndex = getFormIndex(formDef, absoluteRef(ref));
        FormEntryPrompt formEntryPrompt = new FormEntryPrompt(formDef, formIndex);
        return formEntryPrompt.getSelectChoices();
    }

    private Object getAnswerValue(FormDef formDef, String ref) {
        FormIndex formIndex = getFormIndex(formDef, absoluteRef(ref));
        FormEntryPrompt formEntryPrompt = new FormEntryPrompt(formDef, formIndex);
        return formEntryPrompt.getAnswerValue().getValue();
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
        tr.setContextType(CONTEXT_ABSOLUTE);
        tr.setInstanceName(null);
        Arrays.stream(path.split("/"))
            .filter(s -> !s.isEmpty())
            .forEach(s -> tr.add(s, INDEX_UNBOUND));
        return tr;
    }
}
