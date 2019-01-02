package org.javarosa.xform.parse;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Path;
import org.javarosa.core.model.FormDef;
import org.javarosa.xform.util.XFormUtils;

public final class FormParserHelper {

    private FormParserHelper() {
    }

    public static FormDef parse(Path formPath) throws IOException {
        return parse(formPath, formPath.getParent());
    }

    public static FormDef parse(Path formName, Path externalInstancePathPrefix) throws IOException {
        return XFormUtils.getFormFromInputStream(
            new FileInputStream(formName.toString()),
            externalInstancePathPrefix.toString()
        );
    }
}
