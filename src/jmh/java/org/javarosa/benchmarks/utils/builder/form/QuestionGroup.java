package org.javarosa.benchmarks.utils.builder.form;

import java.util.List;

public  class QuestionGroup implements IsNode {

    private String name;
    private List<Question> questions;

    public QuestionGroup(String name, List<Question> questions) {
        this.name = name;
        this.questions = questions;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<Question> getQuestions() {
        return questions;
    }

    @Override
    public String getTagName() {
        return "group";
    }
}
