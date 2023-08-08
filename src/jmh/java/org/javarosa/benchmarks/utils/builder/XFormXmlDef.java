package org.javarosa.benchmarks.utils.builder;

import org.javarosa.benchmarks.utils.builder.form.ChoiceSelector;
import org.javarosa.benchmarks.utils.builder.form.Question;
import org.javarosa.benchmarks.utils.builder.form.QuestionGroup;

import java.util.List;
import java.util.Map;

/**
 * Defines main components of the XForm
 * being used to generate the xform XML file
 */
public  class XFormXmlDef {

    static final Map<String, String> DEFAULT_NAMESPACES =
        XFormBuilder.buildMap(
            new String[]{"xmlns", "http://www.w3.org/2002/xforms"},
            new String[]{"xmlns:h", "http://www.w3.org/1999/xhtml"},
            new String[]{"xmlns:ev", "http://www.w3.org/2001/xml-events"},
            new String[]{"xmlns:jr", "http://openrosa.org/javarosa"},
            new String[]{"xmlns:odk", "http://www.opendatakit.org/xforms"},
                new String[]{"xmlns:orx", "http://openrosa.org/xforms"},
            new String[]{"xmlns:xsd", "http://www.w3.org/2001/XMLSchema"}
        );

    private String title;
    private String formId;
    private List<Question> questions;
    private List<QuestionGroup> questionGroups;
    private List<ChoiceSelector> internalChoiceSelectorList;
    private List<ChoiceSelector> externalChoiceSelectorList;

    public XFormXmlDef(String title, List<QuestionGroup> questionGroups, List<Question> questions,
                List<ChoiceSelector> internalChoiceSelectorList, List<ChoiceSelector> externalChoiceSelectorList) {
        this.title = title;
        this.questions = questions;
        this.questionGroups = questionGroups;
        this.internalChoiceSelectorList = internalChoiceSelectorList;
        this.externalChoiceSelectorList = externalChoiceSelectorList;
        this.formId = "form_" + System.currentTimeMillis();
    }

    private String getFormId() {
        return formId;
    }

    String getTitle() {
        return title;
    }

    List<Question> getQuestions() {
        return questions;
    }

    List<QuestionGroup> getQuestionGroups() {
        return questionGroups;
    }

    List<ChoiceSelector> getExternalChoiceSelectorList() {
        return externalChoiceSelectorList;
    }

    Map<String, String> getNamespaces() {
        return DEFAULT_NAMESPACES;
    }

    List<ChoiceSelector> getInternalChoiceSelectorList() {
        return internalChoiceSelectorList;
    }

    //Non POJO methods
    String getMainInstanceTagName(){
        return getFormId().toLowerCase()
            .replace(" ", "_")
            .replace("-", "_");
    }

    String[] getIdAttribute(){
        return new String[]{"id", getFormId()};
    }
}
