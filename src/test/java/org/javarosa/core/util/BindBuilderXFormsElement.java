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

package org.javarosa.core.util;

import java.util.HashMap;
import java.util.Map;

public class BindBuilderXFormsElement implements XFormsElement {
    private final Map<String, String> attributes = new HashMap<>();

    private BindBuilderXFormsElement(String nodeset) {
        attributes.put("nodeset", nodeset);
    }

    public String getNodeset() {
        return attributes.getOrDefault("nodeset", "");
    }

    public static BindBuilderXFormsElement bind(String nodeset) {
        return new BindBuilderXFormsElement(nodeset);
    }

    public BindBuilderXFormsElement type(String type) {
        attributes.put("type", type);
        return this;
    }

    public BindBuilderXFormsElement constraint(String expression) {
        attributes.put("constraint", expression);
        return this;
    }

    public BindBuilderXFormsElement required() {
        String expression = "true()";
        return required(expression);
    }

    public BindBuilderXFormsElement required(String expression) {
        attributes.put("required", expression);
        return this;
    }


    public BindBuilderXFormsElement relevant(String expression) {
        attributes.put("relevant", expression);
        return this;
    }

    public BindBuilderXFormsElement calculate(String expression) {
        attributes.put("calculate", expression);
        return this;
    }

    public BindBuilderXFormsElement preload(String expression) {
        attributes.put("jr:preload", expression);
        return this;
    }

    public BindBuilderXFormsElement readonly() {
        attributes.put("readonly", "true()");
        return this;
    }

    public BindBuilderXFormsElement readonly(String expression) {
        attributes.put("readonly", expression);
        return this;
    }

    public BindBuilderXFormsElement withAttribute(String namespace, String name, String expression) {
        attributes.put(namespace + ":" + name, expression);
        return this;
    }

    @Override
    public String getName() {
        return "bind";
    }

    @Override
    public String asXml() {
        return new EmptyXFormsElement("bind", attributes).asXml();
    }
}
