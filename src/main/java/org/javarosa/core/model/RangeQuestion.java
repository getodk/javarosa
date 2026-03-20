package org.javarosa.core.model;

import org.javarosa.core.model.instance.TreeReference;
import org.javarosa.core.util.externalizable.DeserializationException;
import org.javarosa.core.util.externalizable.ExtUtil;
import org.javarosa.core.util.externalizable.ExtWrapNullable;
import org.javarosa.core.util.externalizable.PrototypeFactory;
import org.javarosa.xform.parse.RangeParser;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/** A Range-type question, with information pulled from attributes of the range form element */
public class RangeQuestion extends QuestionDef {

    private BigDecimal rangeStart;
    private BigDecimal rangeEnd;
    private BigDecimal rangeStep;
    private BigDecimal tickInterval;
    private BigDecimal placeholder;

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

    public void setTickInterval(BigDecimal tickInterval) {
        this.tickInterval = tickInterval;
    }

    public BigDecimal getTickInterval() {
        return tickInterval;
    }

    public void setPlaceholder(BigDecimal placeholder) {
        this.placeholder = placeholder;
    }

    public BigDecimal getPlaceholder() {
        return placeholder;
    }

    @Override
    public void readExternal(DataInputStream dis, PrototypeFactory pf) throws IOException, DeserializationException {
        super.readExternal(dis, pf);
        rangeStart = RangeParser.getDecimalValue((String) ExtUtil.read(dis, new ExtWrapNullable(String.class), pf));
        rangeEnd = RangeParser.getDecimalValue((String) ExtUtil.read(dis, new ExtWrapNullable(String.class), pf));
        rangeStep = RangeParser.getDecimalValue((String) ExtUtil.read(dis, new ExtWrapNullable(String.class), pf));
        tickInterval = RangeParser.getDecimalValue((String) ExtUtil.read(dis, new ExtWrapNullable(String.class), pf));
        placeholder = RangeParser.getDecimalValue((String) ExtUtil.read(dis, new ExtWrapNullable(String.class), pf));
    }

    @Override
    public void writeExternal(DataOutputStream dos) throws IOException {
        super.writeExternal(dos);
        ExtUtil.write(dos, new ExtWrapNullable(rangeStart != null ? rangeStart.toString() : null));
        ExtUtil.write(dos, new ExtWrapNullable(rangeEnd != null ? rangeEnd.toString() : null));
        ExtUtil.write(dos, new ExtWrapNullable(rangeStep != null ? rangeStep.toString() : null));
        ExtUtil.write(dos, new ExtWrapNullable(tickInterval != null ? tickInterval.toString() : null));
        ExtUtil.write(dos, new ExtWrapNullable(placeholder != null ? placeholder.toString() : null));
    }

    @Override
    public void updateQuestionAnswerInModel(FormDef formDef, TreeReference curQRef, List<SelectChoice> choices, Map<String, SelectChoice> selectChoicesForAnswer) {
        // Do not perform any updates for RangeQuestion with choices
    }
}
