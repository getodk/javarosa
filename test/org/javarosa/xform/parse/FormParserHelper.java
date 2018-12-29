package org.javarosa.xform.parse;

import org.javarosa.core.model.FormDef;
import org.javarosa.xform.util.XFormUtils;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public final class FormParserHelper {

    private FormParserHelper() {
    }

    public static ParseResult parse(Path formPath) throws IOException {
        return parse(formPath, formPath.getParent());
    }

    public static ParseResult parse(Path formName, Path externalInstancePathPrefix) throws IOException {
        final List<String> errorMessages = new ArrayList<>();
        FormDef formDef = XFormUtils.getFormFromInputStream(new FileInputStream(formName.toString()),
            externalInstancePathPrefix.toString(), errorMessages);
        return new ParseResult(formDef, errorMessages);
    }

    public static class ParseResult {
        public final FormDef formDef;
        public final List<String> errorMessages;

        ParseResult(FormDef formDef, List<String> errorMessages) {
            this.formDef = formDef;
            this.errorMessages = errorMessages;
        }
    }
}
