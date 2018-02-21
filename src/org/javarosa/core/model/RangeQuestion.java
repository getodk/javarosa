package org.javarosa.core.model;

import java.math.BigDecimal;

/** A Range-type question, with information pulled from attributes of the range form element */
public class RangeQuestion extends QuestionDef {

    private BigDecimal rangeStart;
    private BigDecimal rangeEnd;
    private BigDecimal rangeStep;
    private String rangeStartLabel;
    private String rangeEndLabel;

    public void setRangeStart(BigDecimal rangeStart) {
        this.rangeStart = rangeStart;
    }

    public BigDecimal getRangeStart() {
        return rangeStart;
    }

    public void setRangeEnd(BigDecimal rangeEnd) {
        this.rangeEnd = rangeEnd;
    }

    public BigDecimal getRangeEnd() {
        return rangeEnd;
    }

    public void setRangeStep(BigDecimal rangeStep) {
        this.rangeStep = rangeStep;
    }

    public BigDecimal getRangeStep() {
        return rangeStep;
    }

    public String getRangeStartLabel() {
        return rangeStartLabel;
    }

    public void setRangeStartLabel(String rangeStartLabel) {
        this.rangeStartLabel = rangeStartLabel;
    }

    public String getRangeEndLabel() {
        return rangeEndLabel;
    }

    public void setRangeEndLabel(String rangeEndLabel) {
        this.rangeEndLabel = rangeEndLabel;
    }
}
