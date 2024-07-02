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

package org.javarosa.test;

import static org.javarosa.test.XFormsElement.buildAttributesString;

import java.util.List;
import java.util.Map;

class TagXFormsElement implements XFormsElement {
    private final String name;
    private final Map<String, String> attributes;
    private final List<XFormsElement> children;

    TagXFormsElement(String name, Map<String, String> attributes, List<XFormsElement> children) {
        assert !children.isEmpty();
        this.name = name;
        this.attributes = attributes;
        this.children = children;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String asXml() {
        String attributesString = buildAttributesString(attributes);
        StringBuilder childrenStringBuilder = new StringBuilder();
        for (XFormsElement e : children)
            childrenStringBuilder.append(e.asXml());
        return String.format(
            "%s<%s%s>%s</%s>",
            name.equals("h:html") ? "<?xml version=\"1.0\"?>" : "",
            name,
            attributesString.isEmpty() ? "" : " " + attributesString,
            childrenStringBuilder,
            name
        );
    }
}
