package org.javarosa.benchmarks.utils.builder;

import org.javarosa.benchmarks.utils.builder.form.Choice;
import org.javarosa.benchmarks.utils.builder.form.ChoiceSelector;
import org.javarosa.benchmarks.utils.builder.form.Question;
import org.javarosa.benchmarks.utils.builder.form.QuestionGroup;
import org.javarosa.benchmarks.utils.builder.form.QuestionType;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import static org.javarosa.benchmarks.utils.builder.Constants.BIND;
import static org.javarosa.benchmarks.utils.builder.Constants.BODY;
import static org.javarosa.benchmarks.utils.builder.Constants.CLOSE_TOKEN;
import static org.javarosa.benchmarks.utils.builder.Constants.DOUBLE_QUOTE;
import static org.javarosa.benchmarks.utils.builder.Constants.EMPTY_STRING;
import static org.javarosa.benchmarks.utils.builder.Constants.EQUALS;
import static org.javarosa.benchmarks.utils.builder.Constants.FORWARD_SLASH;
import static org.javarosa.benchmarks.utils.builder.Constants.GROUP;
import static org.javarosa.benchmarks.utils.builder.Constants.HEAD;
import static org.javarosa.benchmarks.utils.builder.Constants.HINT;
import static org.javarosa.benchmarks.utils.builder.Constants.HTML;
import static org.javarosa.benchmarks.utils.builder.Constants.INPUT_TEXT;
import static org.javarosa.benchmarks.utils.builder.Constants.INSTANCE;
import static org.javarosa.benchmarks.utils.builder.Constants.ITEM;
import static org.javarosa.benchmarks.utils.builder.Constants.ITEM_SET;
import static org.javarosa.benchmarks.utils.builder.Constants.LABEL;
import static org.javarosa.benchmarks.utils.builder.Constants.MODEL;
import static org.javarosa.benchmarks.utils.builder.Constants.NEW_LINE;
import static org.javarosa.benchmarks.utils.builder.Constants.NODE_SET;
import static org.javarosa.benchmarks.utils.builder.Constants.OPEN_TOKEN;
import static org.javarosa.benchmarks.utils.builder.Constants.REF;
import static org.javarosa.benchmarks.utils.builder.Constants.SPACE;
import static org.javarosa.benchmarks.utils.builder.Constants.TITLE;
import static org.javarosa.benchmarks.utils.builder.Constants.VALUE;

public class XFormBuilder {

    private StringBuilder stringBuilder;
    private XFormXmlDef XFormXmlDef;
    private Path workingDirectory;
    Map<String, Path> externalSecondaryInstances;
    private boolean minify;

    public XFormBuilder(XFormXmlDef XFormXmlDef, Path workingDirectory) {
        stringBuilder = new StringBuilder("<?xml version=\"1.0\"?>\n");
        this.XFormXmlDef = XFormXmlDef;
        this.workingDirectory = workingDirectory;
        this.minify = false;
    }

    public String build() {
        return
            // formatXML(
            buildHtml()
                .buildHead()
                .buildBody()
                .buildTitle()
                .buildModel()
                .buildPrimaryInstance()
                .buildInternalSecondaryInstances()
                .buildExternalSecondaryInstances()
                .buildBind()
                .buildControls()
                .toString();
        //);
    }

    public Map<String, Path> buildExternalInstances() {
        return
            buildHtml()
                .buildHead()
                .buildBody()
                .buildTitle()
                .buildModel()
                .buildPrimaryInstance()
                .buildInternalSecondaryInstances()
                .buildExternalSecondaryInstances()
                .buildBind()
                .buildControls()
                .getExternalSecondaryInstances();
    }

    private XFormBuilder buildHtml() {
        if (!hasHtml()) {
            String htmlElementString = openAndClose(HTML, XFormXmlDef.getNamespaces());
            stringBuilder.append(htmlElementString);
        }
        return this;
    }

    private XFormBuilder buildHead() {
        addChild(HTML, openAndClose(HEAD));
        return this;
    }

    private XFormBuilder buildBody() {
        addChild(HTML, openAndClose(BODY));
        return this;
    }

    private XFormBuilder buildTitle() {
        addChild(HEAD, openAndClose(TITLE, null, XFormXmlDef.getTitle()));
        return this;
    }

    private XFormBuilder buildModel() {
        addChild(HEAD, openAndClose(MODEL));
        return this;
    }

    static Map<String, String> buildMap(String[]... args) {
        Map<String, String> map = new HashMap<>();
        for (String[] pair : args) map.put(pair[0], pair[1]);
        return map;
    }

    private XFormBuilder buildPrimaryInstance() {
        final String ROOT = XFormXmlDef.getMainInstanceTagName();
        final String primaryInstanceString =
            new StringBuilder(openingTag(INSTANCE))
                .append(openingTag(ROOT, buildMap(XFormXmlDef.getIdAttribute())))
                .append(shortOpenAndClose("start"))
                .append(shortOpenAndClose("end"))
                .append(shortOpenAndClose("today"))
                .append(shortOpenAndClose("deviceid"))
                .append(shortOpenAndClose("subscriberid"))
                .append(shortOpenAndClose("simserial"))
                .append(shortOpenAndClose("phonenumber"))
                .append(generateQuestionGroup(XFormXmlDef.getQuestionGroups()))
                .append(generateQuestions(XFormXmlDef.getQuestions()))
                .append(closingTag(ROOT))
                .append(closingTag(INSTANCE))
                .toString();
        addChild(MODEL, primaryInstanceString);
        return this;
    }

    private XFormBuilder buildInternalSecondaryInstances() {
        for (ChoiceSelector choiceSelector : XFormXmlDef.getInternalChoiceSelectorList()) {
            StringBuilder sb = new StringBuilder();
            final Map<String, String> idAttr = buildMap(new String[]{"id", choiceSelector.getInstanceId()});
            sb.append(openingTag(INSTANCE, idAttr))
                .append(openingTag(choiceSelector.getInstanceId()))
                .append(generateSecondaryInstanceOptions(choiceSelector))
                .append(closingTag(choiceSelector.getInstanceId()))
                .append(closingTag(INSTANCE));

            addChild(MODEL, sb.toString());
        }
        return this;
    }

    private XFormBuilder buildExternalSecondaryInstances() {
        generateExternalInstanceFiles(XFormXmlDef.getExternalChoiceSelectorList());
        for (ChoiceSelector choiceSelector : XFormXmlDef.getExternalChoiceSelectorList()) {
            String instanceId = choiceSelector.getInstanceId();
            final Map<String, String> attributesMap = buildMap(
                new String[]{"id", choiceSelector.getInstanceId()},
                new String[]{"src", "jr://file/" + externalSecondaryInstances.get(instanceId).toFile().getName()}
            );
            addChild(MODEL, openAndClose(INSTANCE, attributesMap));
        }
        return this;
    }

    private XFormBuilder buildBind() {
        List<QuestionGroup> questionGroups = XFormXmlDef.getQuestionGroups();
        for (QuestionGroup questionGroup : questionGroups) {
            for (Question question : questionGroup.getQuestions()) {
                String nodeset = generatePath(XFormXmlDef.getMainInstanceTagName(),
                    questionGroup.getName(),
                    question.getTagName()
                );
                Map<String, String> attrs = buildMap(
                    new String[]{NODE_SET, nodeset},
                    new String[]{"type", "string"}
                );
                addChild(MODEL, shortOpenAndClose(BIND, attrs));
            }
        }

        for (Question question : XFormXmlDef.getQuestions()) {
            String nodeset = generatePath(XFormXmlDef.getMainInstanceTagName(), question.getTagName());
            Map<String, String> attrs = buildMap(
                new String[]{NODE_SET, nodeset},
                new String[]{"type", "string"}
            );
            addChild(MODEL, shortOpenAndClose(BIND, attrs));
        }
        return this;
    }

    private XFormBuilder buildControls() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(
            buildControl(XFormXmlDef.getQuestions(),
                XFormXmlDef.getMainInstanceTagName())
        );
        List<QuestionGroup> questionGroups = XFormXmlDef.getQuestionGroups();
        for (QuestionGroup questionGroup : questionGroups) {
            stringBuilder.append(openingTag(GROUP, buildMap(new String[]{"appearance", "field-list"})));
            stringBuilder.append(
                buildControl(questionGroup.getQuestions(),
                    generatePath(XFormXmlDef.getMainInstanceTagName(), questionGroup.getName()))
            );
            stringBuilder.append(closingTag(GROUP));
        }
        addChild(BODY, stringBuilder.toString());
        return this;
    }

    private String buildControl(List<Question> questions, String parentNode) {
        StringBuilder stringBuilder = new StringBuilder();
        for (Question question : questions) {
            String ref = generatePath(parentNode, question.getTagName());
            String controlTag = INPUT_TEXT;
            Map<String, String> attributes = buildMap(new String[]{REF, ref});
            stringBuilder
                .append(openingTag(controlTag, attributes))
                .append(openAndClose(LABEL, null, question.getLabel()))
                .append(openAndClose(HINT, null, question.getHint()));

            if (question.getQuestionType().equals(QuestionType.TEXT)) {
                String instanceId = question.getOptionSelector().getInstanceId();
                String instanceSelector = "instance('" + instanceId + "')";
                String nodeset = generatePath(false, instanceSelector, instanceId, ITEM);
                stringBuilder
                    .append(openingTag(ITEM_SET, buildMap(new String[]{NODE_SET, nodeset})))
                    .append(shortOpenAndClose(VALUE, buildMap(new String[]{REF, VALUE})))
                    .append(shortOpenAndClose(LABEL, buildMap(new String[]{REF, LABEL})))
                    .append(closingTag(ITEM_SET));
            }
            stringBuilder
                .append(closingTag(controlTag));

        }
        return stringBuilder.toString();
    }


    public Map<String, Path> getExternalSecondaryInstances() {
        return externalSecondaryInstances;
    }

    private void generateExternalInstanceFiles(List<ChoiceSelector> choiceSelectorList) {
        externalSecondaryInstances = new HashMap<>();
        try {
            for (ChoiceSelector choiceSelector : choiceSelectorList) {
                StringBuilder sb = new StringBuilder();
                String instanceId = choiceSelector.getInstanceId();
                sb.append(openingTag(instanceId))
                    .append(generateSecondaryInstanceOptions(choiceSelector))
                    .append(closingTag(instanceId));
                File externalInstanceFile = new File(workingDirectory + File.separator + instanceId + ".xml");
                FileWriter fileWriter = new FileWriter(externalInstanceFile);
                fileWriter.write(sb.toString());
                fileWriter.close();
                externalSecondaryInstances.put(choiceSelector.getInstanceId(), externalInstanceFile.toPath());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String generateQuestionGroup(List<QuestionGroup> questionGroupList) {
        if (questionGroupList != null) {
            StringBuilder stringBuilder = new StringBuilder();
            for (QuestionGroup questionGroup : questionGroupList) {
                stringBuilder
                    .append(openingTag(questionGroup.getName()))
                    .append(generateQuestions(questionGroup.getQuestions()))
                    .append(closingTag(questionGroup.getName()));
            }
            return stringBuilder.toString();
        }
        return EMPTY_STRING;
    }

    private String generatePath (String ...parts){
        return generatePath(true, parts);
    }

    private String generatePath ( boolean absolute, String ...parts){
        return ((absolute ? FORWARD_SLASH : EMPTY_STRING) + String.join(FORWARD_SLASH, parts)).replaceAll("//", FORWARD_SLASH);
    }

    private String generateSecondaryInstanceOptions (ChoiceSelector choiceSelector){
        StringBuilder stringBuilder = new StringBuilder();
        for (Choice choice : choiceSelector.getItems()) {
            stringBuilder
                .append(openingTag(ITEM))
                .append(openAndClose(LABEL, null, choice.getLabel()))
                .append(openAndClose(VALUE, null, choice.getValue()))
                .append(closingTag(ITEM));
        }
        return stringBuilder.toString();
    }

    private String generateQuestions (List < Question > questions) {
        StringBuilder stringBuilder = new StringBuilder();
        for (Question question : questions) {
            String realTagName = question.getTagName();
            stringBuilder.append(shortOpenAndClose(realTagName));
        }
        return stringBuilder.toString();
    }


    private String generateAttributes(Map < String, String > attributes){
        if (attributes != null) {
            StringBuilder stringBuilder = new StringBuilder();
            Iterator<Map.Entry<String, String>> iterator = attributes.entrySet().iterator();
            while (iterator.hasNext()) {
                Map.Entry<String, String> pair = iterator.next();
                stringBuilder.append(generateAttribute(pair.getKey(), pair.getValue()));
                stringBuilder.append(iterator.hasNext() ? SPACE : EMPTY_STRING);
            }
            return stringBuilder.toString();
        }
        return EMPTY_STRING;
    }

    private String generateAttribute(String key, String value){
        return key + EQUALS + DOUBLE_QUOTE + value + DOUBLE_QUOTE;
    }

        private boolean hasHtml () {
            return false;
        }

        private String shortOpenAndClose (String name, Map < String, String > attributes){
            return OPEN_TOKEN +
                name +
                SPACE +
                generateAttributes(attributes) +
                FORWARD_SLASH + CLOSE_TOKEN +
                newLine();
        }

        private String shortOpenAndClose (String name){
            return OPEN_TOKEN + name + FORWARD_SLASH + CLOSE_TOKEN + newLine();
        }

        private String openAndClose (String name){
            return openingTag(name) + closingTag(name);
        }

        private String openAndClose (String name, Map < String, String > attributes){
            return openingTag(name, attributes) + closingTag(name);
        }

        private String openAndClose (String name, Map < String, String > attributes, String xmlText){
            return openingTag(name, attributes) +
                xmlText +
                NEW_LINE +
                closingTag(name);
        }

        private String openingTag (String name){
            return OPEN_TOKEN + name + CLOSE_TOKEN + newLine();
        }

        private String openingTag (String name, Map < String, String > attributes){
            return
                new StringBuilder(OPEN_TOKEN)
                    .append(name)
                    .append(attributes == null ? EMPTY_STRING : SPACE)
                    .append(generateAttributes(attributes))
                    .append(CLOSE_TOKEN)
                    .append(NEW_LINE)
                    .toString();
        }

        private String closingTag (String name){
            return OPEN_TOKEN + FORWARD_SLASH + name + CLOSE_TOKEN + newLine();
        }

        private void addChild (String parentName, String childString){
            String CLOSING_TAG_TOKEN = closingTag(parentName);
            int insertionIndex = stringBuilder.indexOf(CLOSING_TAG_TOKEN);
            stringBuilder.insert(insertionIndex, childString);
        }

        private String newLine () {
            return minify ? EMPTY_STRING : NEW_LINE;
        }

        public String toString () {
            return stringBuilder.toString();
        }

        public String formatXML (){
            String unformattedXML = stringBuilder.toString();
            final int length = unformattedXML.length();
            final int indentSpace = 3;
            final StringBuilder newString = new StringBuilder(length + length / 10);
            final char space = ' ';
            int i = 0;
            int indentCount = 0;
            char currentChar = unformattedXML.charAt(i++);
            char previousChar = currentChar;
            boolean nodeStarted = true;
            newString.append(currentChar);
            for (; i < length - 1; ) {
                currentChar = unformattedXML.charAt(i++);
                if (((int) currentChar < 33) && !nodeStarted) {
                    continue;
                }
                switch (currentChar) {
                    case '<':
                        if ('>' == previousChar && '/' != unformattedXML.charAt(i - 1) && '/' != unformattedXML.charAt(i) && '!' != unformattedXML.charAt(i)) {
                            indentCount++;
                        }
                        newString.append(System.lineSeparator());
                        for (int j = indentCount * indentSpace; j > 0; j--) {
                            newString.append(space);
                        }
                        newString.append(currentChar);
                        nodeStarted = true;
                        break;
                    case '>':
                        newString.append(currentChar);
                        nodeStarted = false;
                        break;
                    case '/':
                        if ('<' == previousChar || '>' == unformattedXML.charAt(i)) {
                            indentCount--;
                        }
                        newString.append(currentChar);
                        break;
                    default:
                        newString.append(currentChar);
                }
                previousChar = currentChar;
            }
            newString.append(unformattedXML.charAt(length - 1));
            return newString.toString();
        }
}