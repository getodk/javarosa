package org.javarosa.core.reference;

import org.javarosa.core.util.externalizable.DeserializationException;
import org.javarosa.core.util.externalizable.ExtUtil;
import org.javarosa.core.util.externalizable.Externalizable;
import org.javarosa.core.util.externalizable.PrototypeFactory;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

/**
 * A Root Translator is a simple reference factory which doesn't
 * actually derive any specific references, but rather translates
 * references from one prefix to another. This is useful for roots
 * which don't describe any real raw accessor like "jr://media/",
 * which could access a file reference (jr://file/) on one platform,
 * but a resource reference (jr://resource/) on another.
 *
 * Root Translators can be externalized and used as a dynamically
 * configured object.
 *
 * @author ctsims
 *
 */
public class RootTranslator implements ReferenceFactory, Externalizable {

    public String prefix;
    public String translatedPrefix;

    /**
     * Serialization only!
     */
    public RootTranslator() {

    }

    /**
     * Creates a translator which will create references of the
     * type described by translatedPrefix whenever references of
     * the type prefix are being derived.
     */
    public RootTranslator(String prefix, String translatedPrefix) {
        //TODO: Manage semantics of "ends with /" etc here?
        this.prefix = prefix;
        this.translatedPrefix = translatedPrefix;
    }

    @Override
    public Reference derive(String URI) throws InvalidReferenceException {
        return ReferenceManager.instance().deriveReference(translatedPrefix + URI.substring(prefix.length()));
    }

    @Override
    public Reference derive(String URI, String context) throws InvalidReferenceException {
        return ReferenceManager.instance().deriveReference(URI, translatedPrefix + context.substring(prefix.length()));
    }

    @Override
    public boolean derives(String URI) {
        return URI.startsWith(prefix) && !URI.startsWith(translatedPrefix);
    }

    @Override
    public void readExternal(DataInputStream in, PrototypeFactory pf)
            throws IOException, DeserializationException {
        prefix = ExtUtil.readString(in);
        translatedPrefix = ExtUtil.readString(in);
    }

    @Override
    public void writeExternal(DataOutputStream out) throws IOException {
        ExtUtil.writeString(out, prefix);
        ExtUtil.writeString(out, translatedPrefix);
    }

    @Override
    public String toString() {
        return "RootTranslator{" +
            "prefix='" + prefix + '\'' +
            ", translatedPrefix='" + translatedPrefix + '\'' +
            '}';
    }
}
