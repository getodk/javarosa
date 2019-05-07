package org.javarosa.core.model;

import org.javarosa.core.util.externalizable.DeserializationException;
import org.javarosa.core.util.externalizable.ExtUtil;
import org.javarosa.core.util.externalizable.ExtWrapNullable;
import org.javarosa.core.util.externalizable.PrototypeFactory;
import org.javarosa.xform.parse.RangeParser;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.math.BigDecimal;

/** A Range-type question, with information pulled from attributes of the range form element */
public class RangeQuestion extends QuestionDef {

    private BigDecimal rangeStart;
    private BigDecimal rangeEnd;
    private BigDecimal rangeStep;

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

    @Override
    public void readExternal(DataInputStream dis, PrototypeFactory pf) throws IOException, DeserializationException {
        super.readExternal(dis, pf);
        rangeStart = RangeParser.getDecimalValue((String) ExtUtil.read(dis, new ExtWrapNullable(String.class), pf));
        rangeEnd = RangeParser.getDecimalValue((String) ExtUtil.read(dis, new ExtWrapNullable(String.class), pf));
        rangeStep = RangeParser.getDecimalValue((String) ExtUtil.read(dis, new ExtWrapNullable(String.class), pf));
    }

    @Override
    public void writeExternal(DataOutputStream dos) throws IOException {
        super.writeExternal(dos);
        ExtUtil.write(dos, new ExtWrapNullable(rangeStart != null ? rangeStart.toString() : null));
        ExtUtil.write(dos, new ExtWrapNullable(rangeEnd != null ? rangeEnd.toString() : null));
        ExtUtil.write(dos, new ExtWrapNullable(rangeStep != null ? rangeStep.toString() : null));
    }
}
