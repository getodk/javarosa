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

import static java.util.Collections.emptyMap;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public interface XFormsElement {
    static String buildAttributesString(Map<String, String> attributes) {
        StringBuilder attributesStringBuilder = new StringBuilder();
        for (Map.Entry<String, String> entry : attributes.entrySet())
            attributesStringBuilder.append(entry.getKey()).append("=\"").append(entry.getValue()).append("\"").append(" ");
        return attributesStringBuilder.toString().trim();
    }

    String getName();

    String asXml();

    static Map<String, String> parseAttributes(String name) {
        if (!name.contains(" "))
            return emptyMap();
        Map<String, String> attributes = new HashMap<>();
        String[] words = name.split(" ");
        for (String word : Arrays.asList(words).subList(1, words.length)) {
            String[] parts = word.split("=");
            attributes.put(parts[0], parts[1].substring(1, parts[1].length() - 1));
        }
        return attributes;
    }

    static String parseName(String name) {
        if (!name.contains(" "))
            return name;
        return name.split(" ")[0];
    }

    static XFormsElement t(String name, XFormsElement... children) {
        if (children.length == 0)
            return new EmptyXFormsElement(parseName(name), parseAttributes(name));
        return new TagXFormsElement(parseName(name), parseAttributes(name), Arrays.asList(children));
    }

    static XFormsElement t(String name, String innerHtml) {
        return new StringLiteralXFormsElement(parseName(name), parseAttributes(name), innerHtml);
    }

    static XFormsElement html(XFormsElement... children) {
        return t("h:html " +
                        "xmlns=\"http://www.w3.org/2002/xforms\" " +
                        "xmlns:h=\"http://www.w3.org/1999/xhtml\" " +
                        "xmlns:jr=\"http://openrosa.org/javarosa\"",
                children
        );
    }

    static XFormsElement head(XFormsElement... children) {
        return t("h:head", children);
    }

    static XFormsElement body(XFormsElement... children) {
        return t("h:body", children);
    }

    static XFormsElement title(String innerHTML) {
        return t("h:title", innerHTML);
    }

    static XFormsElement model(XFormsElement... children) {
        return t("model", children);
    }

    static XFormsElement mainInstance(XFormsElement... children) {
        return t("instance", children);
    }


    static XFormsElement input(String ref, XFormsElement... children) {
        return t("input ref=\"" + ref + "\"", children);
    }

    static XFormsElement select1(String ref, XFormsElement... children) {
        return t("select1 ref=\"" + ref + "\"", children);
    }

    static XFormsElement group(String ref, XFormsElement... children) {
        return t("group ref=\"" + ref + "\"", children);
    }

    static XFormsElement repeat(String ref, XFormsElement... children) {
        return t("repeat nodeset=\"" + ref + "\"", children);
    }

    static XFormsElement repeat(String ref, String countRef, XFormsElement... children) {
        return t("repeat nodeset=\"" + ref + "\" jr:count=\"" + countRef + "\"", children);
    }

    static XFormsElement label(String innerHtml) {
        return new StringLiteralXFormsElement("label", emptyMap(), innerHtml);
    }

    static XFormsElement item(int value, String label) {
        return item(String.valueOf(value), label);
    }

    static XFormsElement item(String value, String label) {
        return t("item",
                t("label", label),
                t("value", value)
        );
    }

    static XFormsElement setvalue(String event, String ref, String value) {
        return t("setvalue event=\"" + event + "\" ref=\"" + ref + "\" value=\"" + value + "\"");
    }

    static XFormsElement setvalueLiteral(String event, String ref, String innerHtml) {
        return t("setvalue event=\"" + event + "\" ref=\"" + ref + "\"", innerHtml);
    }
}
