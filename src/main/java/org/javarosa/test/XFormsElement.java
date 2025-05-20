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

import kotlin.Pair;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.regex.Pattern;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyMap;

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

        // Regex to split on spaces, ignoring spaces inside quoted text
        final String SPACE_OUTSIDE_QUOTES_REGEX = " (?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)";
        Pattern spaceOutsideQuotesPattern = Pattern.compile(SPACE_OUTSIDE_QUOTES_REGEX);
        String[] words = spaceOutsideQuotesPattern.split(name);

        for (String word : asList(words).subList(1, words.length)) {
            String[] parts = word.split("(?<!\\))=(\"|')", 2);
            attributes.put(parts[0], parts[1].substring(0, parts[1].length() - 1));
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
        return new TagXFormsElement(parseName(name), parseAttributes(name), asList(children));
    }

    static XFormsElement t(String name, String innerHtml) {
        return new StringLiteralXFormsElement(parseName(name), parseAttributes(name), innerHtml);
    }

    static XFormsElement html(HeadXFormsElement head, BodyXFormsElement body) {
        return t("h:html " +
                        "xmlns=\"http://www.w3.org/2002/xforms\" " +
                        "xmlns:h=\"http://www.w3.org/1999/xhtml\" " +
                        "xmlns:jr=\"http://openrosa.org/javarosa\" " +
                        "xmlns:odk=\"http://www.opendatakit.org/xforms\" "+
                        "xmlns:orx=\"http://openrosa.org/xforms\"",
                head, body
        );
    }

    static XFormsElement html(List<Pair<String, String>> additionalNamespaces, HeadXFormsElement head, BodyXFormsElement body) {
        String additionalNamespacesString = additionalNamespaces.stream()
            .map(namespace -> "xmlns:" + namespace.getFirst() + "=\"" + namespace.getSecond() + "\" ")
            .collect(Collectors.joining());

        return t("h:html " +
                "xmlns=\"http://www.w3.org/2002/xforms\" " +
                "xmlns:h=\"http://www.w3.org/1999/xhtml\" " +
                "xmlns:jr=\"http://openrosa.org/javarosa\" " +
                "xmlns:odk=\"http://www.opendatakit.org/xforms\" " +
                "xmlns:orx=\"http://openrosa.org/xforms\" " +
                additionalNamespacesString,
            head, body
        );
    }

    static HeadXFormsElement head(XFormsElement... children) {
        return new HeadXFormsElement(children);
    }

    static BodyXFormsElement body(XFormsElement... children) {
        return new BodyXFormsElement(children);
    }

    static XFormsElement title(String innerHTML) {
        return t("h:title", innerHTML);
    }

    static XFormsElement model(XFormsElement... children) {
        return t("model", children);
    }

    static XFormsElement model(List<Pair<String, String>> attributes, XFormsElement... children) {
        StringBuilder stringBuilder = new StringBuilder();
        attributes.stream().forEach(attribute -> {
            stringBuilder.append(" " + attribute.getFirst() + "=\"" + attribute.getSecond() + "\"");
        });

        return t("model" + stringBuilder, children);
    }

    static XFormsElement mainInstance(XFormsElement... children) {
        return t("instance", children);
    }

    static XFormsElement instance(String name, XFormsElement... children) {
        return t("instance id=\"" + name + "\"", t("root", children));
    }

    static XFormsElement input(String ref, XFormsElement... children) {
        return t("input ref=\"" + ref + "\"", children);
    }

    static XFormsElement select1(String ref, XFormsElement... children) {
        return t("select1 ref=\"" + ref + "\"", children);
    }

    static XFormsElement select1Dynamic(String ref, String nodesetRef) {
        return select1Dynamic(ref, nodesetRef, "value", "label");
    }

    static XFormsElement select1Dynamic(String ref, String nodesetRef, String valueRef, String labelRef) {
        return t("select1 ref=\"" + ref + "\"",
            t("itemset nodeset=\"" + nodesetRef + "\"",
                t("value ref=\"" + valueRef + "\""),
                t("label ref=\"" + labelRef + "\"")));
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

    static XFormsElement setvalue(String event, String ref) {
        return t("setvalue event=\"" + event + "\" ref=\"" + ref + "\"");
    }

    static XFormsElement setvalueLiteral(String event, String ref, String innerHtml) {
        return t("setvalue event=\"" + event + "\" ref=\"" + ref + "\"", innerHtml);
    }

    class HeadXFormsElement extends TagXFormsElement {
        public HeadXFormsElement(XFormsElement[] children) {
            super("h:head", emptyMap(), asList(children));
        }
    }

    class BodyXFormsElement extends TagXFormsElement {
        public BodyXFormsElement(XFormsElement[] children) {
            super("h:body", emptyMap(), asList(children));
        }
    }
}
