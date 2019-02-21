package org.javarosa.xform.parse;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Path;
import org.javarosa.core.model.FormDef;
import org.javarosa.xform.util.XFormUtils;

public final class FormParserHelper {

    private FormParserHelper() {
    }

    public static FormDef parse(Path formName) throws IOException {
        return XFormUtils.getFormFromInputStream(new FileInputStream(formName.toString()));
    }

    public static FormDef parse(Path formName, String lastSavedSrc) throws IOException {
        return XFormUtils.getFormFromInputStream(new FileInputStream(formName.toString()), lastSavedSrc);
    }
}
