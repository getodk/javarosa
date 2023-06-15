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

package org.javarosa.core.model.data;

import org.javarosa.core.model.data.helper.Selection;
import org.javarosa.core.util.externalizable.DeserializationException;
import org.javarosa.core.util.externalizable.ExtUtil;
import org.javarosa.core.util.externalizable.ExtWrapList;
import org.javarosa.core.util.externalizable.PrototypeFactory;
import org.jetbrains.annotations.NotNull;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static org.javarosa.core.model.utils.StringUtils.split;

/**
 * A response to a question requesting a selection of
 * any number of items from a list or ordering them.
 *
 * @author Drew Roos
 */
public class MultipleItemsData implements IAnswerData {
    private List<Selection> vs; //List of Selection

    /**
     * Empty Constructor, necessary for dynamic construction during deserialization.
     * Shouldn't be used otherwise.
     */
    public MultipleItemsData() {

    }

    public MultipleItemsData(List<Selection> vs) {
        setValue(vs);
    }

    @Override
    public IAnswerData clone() {
        List<Selection> v = new ArrayList<>(vs.size());
        for (Selection v1 : vs) {
            v.add(v1.clone());
        }
        return new MultipleItemsData(v);
    }

    @Override
    public void setValue(Object o) {
        if (o == null) {
            throw new NullPointerException("Attempt to set an IAnswerData class to null.");
        }

        ArrayList<Selection> selections = new ArrayList<>(((List<Object>) o).size());
        for (Object obj : (List<Object>) o) {
            selections.add((Selection) obj);
        }
        vs = selections;
    }

    @Override
    public @NotNull Object getValue() {
        return new ArrayList<>(vs);
    }

    /**
     * @return THE XMLVALUE!!
     */
    @Override
    public String getDisplayText() {
        StringBuilder b = new StringBuilder();

        for (int i = 0; i < vs.size(); i++) {
            Selection s = vs.get(i);
            b.append(s.getValue());
            if (i < vs.size() - 1)
                b.append(", ");
        }

        return b.toString();
    }

    @SuppressWarnings("unchecked")
    @Override
    public void readExternal(DataInputStream in, PrototypeFactory pf) throws IOException, DeserializationException {
        vs = (List<Selection>) ExtUtil.read(in, new ExtWrapList(Selection.class), pf);
    }

    @Override
    public void writeExternal(DataOutputStream out) throws IOException {
        ExtUtil.write(out, new ExtWrapList(vs));
    }

    @Override
    public UncastData uncast() {
        StringBuilder selectString = new StringBuilder();

        for (Selection selection : vs) {
            if (selectString.length() > 0)
                selectString.append(" ");
            selectString.append(selection.getValue());
        }
        //As Crazy, and stupid, as it sounds, this is the XForms specification
        //for storing multiple selections.
        return new UncastData(selectString.toString());
    }

    @Override
    public MultipleItemsData cast(UncastData data) throws IllegalArgumentException {

        List<String> choices = split(data.value, " ", true);
        List<Selection> v = new ArrayList<>(choices.size());

        for (String s : choices) {
            v.add(new Selection(s));
        }
        return new MultipleItemsData(v);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MultipleItemsData that = (MultipleItemsData) o;
        return Objects.equals(vs, that.vs);
    }

    @Override
    public int hashCode() {
        return Objects.hash(vs);
    }
}
