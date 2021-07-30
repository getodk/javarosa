package org.javarosa.benchmarks.utils.builder.form;

/**
 * Abstracts the controls of the XForm
 */
public class Question implements IsNode {

    private String tagName;
    private QuestionType questionType;
    private String label;
    private String hint;
    private ChoiceSelector options;

    public Question(QuestionType questionType, String label) {
        this(questionType, label, "");
    }

    public Question(QuestionType questionType, String label, String hint) {
        this(questionType, label,  hint, null);
    }

    public Question(QuestionType questionType, String label, String hint, ChoiceSelector options) {
        this(questionType, generateTagName(label) , label, hint,  options);
    }

    public Question(QuestionType questionType, String tagName, String label, String hint, ChoiceSelector options) {
        this.tagName = tagName;
        this.questionType = questionType;
        this.label = label;
        this.hint = hint;
        this.options = options;
    }

    public String getTagName() {
        return tagName;
    }

    public QuestionType getQuestionType() {
        return questionType;
    }

    public String getLabel() {
        return label;
    }

    public String getHint() {
        return hint;
    }

    public ChoiceSelector getOptionSelector() {
        return options;
    }

    private static String generateTagName(String label){
        String tagName = label
            .toLowerCase()
            .replaceAll("[^_0-9a-zA-Z]+", "_");
        int lastIndex = tagName.length() - 1;
        return tagName.substring(lastIndex).equals("_") ? tagName.substring(0, lastIndex) : tagName;
    }

}
