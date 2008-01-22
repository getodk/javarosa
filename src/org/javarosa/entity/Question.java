package org.javarosa.entity;

public class Question {
    
    //No enums in J2ME, here are our types
    public final static int TEXT = 0;
    public final static int NUMBER = 1;
    public final static int SINGLE_SELECT = 2;
    public final static int MULTIPLE_SELECT = 3;
    
    public String longText;
    public String shortText;
    public String[] options;
    public int inputType;
    
    public Object value;
    
    public Question(String longText, String shortText, int inputType) {
        this.longText = longText;
        this.shortText = shortText;
        this.inputType = inputType;
    }
    
    public Question(String longText, String shortText, int inputType, String[] options) {
        this.longText = longText;
        this.shortText = shortText;
        this.inputType = inputType;
        this.options = options;
    }
}
