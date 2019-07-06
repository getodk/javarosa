package org.javarosa.xform.parse;

import org.javarosa.core.model.CoreModelModule;
import org.javarosa.core.model.FormDef;
import org.javarosa.core.services.PrototypeManager;
import org.javarosa.core.util.JavaRosaCoreModule;
import org.javarosa.core.util.externalizable.DeserializationException;
import org.javarosa.model.xform.XFormsModule;
import org.javarosa.xform.util.XFormUtils;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.javarosa.core.util.externalizable.ExtUtil.defaultPrototypes;

public final class FormParserHelper {

    private FormParserHelper() {
    }

    public static FormDef parse(Path formName) throws IOException {
        return XFormUtils.getFormFromInputStream(new FileInputStream(formName.toString()));
    }

    public static FormDef parse(Path formName, String lastSavedSrc) throws IOException {
        return XFormUtils.getFormFromInputStream(new FileInputStream(formName.toString()), lastSavedSrc);
    }

    static void serAndDeserializeForm(Path formName) throws IOException, DeserializationException {
        initSerialization();
        FormDef formDef = parse(formName);
        Path p = Files.createTempFile("serialized-form", null);

        final DataOutputStream dos = new DataOutputStream(Files.newOutputStream(p));
        formDef.writeExternal(dos);
        dos.close();

        final DataInputStream dis = new DataInputStream(Files.newInputStream(p));
        formDef.readExternal(dis, defaultPrototypes());
        dis.close();

        Files.delete(p);
    }

    private static void initSerialization() {
        PrototypeManager.registerPrototypes(JavaRosaCoreModule.classNames);
        PrototypeManager.registerPrototypes(CoreModelModule.classNames);
        new XFormsModule().registerModule();
    }
}
