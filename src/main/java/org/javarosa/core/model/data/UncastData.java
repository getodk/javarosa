package org.javarosa.core.model.data;

import org.javarosa.core.util.externalizable.ExtUtil;
import org.javarosa.core.util.externalizable.PrototypeFactory;
import org.jetbrains.annotations.NotNull;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

/**
 * Uncast data values are those which are not assigned a particular
 * data type. This is relevant when data is read before a datatype is
 * available, or when it must be pulled from external instances.
 *
 * In general, Uncast data should be used when a value is available
 * in string form, and no adequate assumption can be made about the type
 * of data being represented. This is preferable to making the assumption
 * that data is a StringData object, since that will cause issues when
 * select choices or other typed values are expected.
 *
 * @author ctsims
 *
 */
public class UncastData implements IAnswerData {
    String value;

    public UncastData() {

    }

    public UncastData(String value) {
        if(value == null) {
            throw new NullPointerException("Attempt to set Uncast Data value to null! IAnswerData objects should never have null values");
        }
        this.value = value;
    }

    @Override
    public IAnswerData clone() {
        return new UncastData(value);
    }

    @Override
    public String getDisplayText() {
        return value;
    }

    @Override
    public @NotNull Object getValue() {
        return value;
    }

    @Override
    public void setValue(@NotNull Object o) {
        value = (String)o;
    }

    /**
     * @return The string representation of this data. This value should be
     * castable into its appropriate data type.
     */
    public String getString() {
        return value;
    }

    @Override
    public void readExternal(DataInputStream in, PrototypeFactory pf)
            throws IOException {
        value = ExtUtil.readString(in);
    }

    @Override
    public void writeExternal(DataOutputStream out) throws IOException {
        ExtUtil.writeString(out, value);
    }

    @Override
    public UncastData uncast() {
        return this;
    }

    @Override
    public UncastData cast(UncastData data) {
        return new UncastData(data.value);
    }
}
