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

    public static ParseResult parse(Path formName) throws IOException {
        return parseWithPrefix(formName, "");
    }

    public static ParseResult parseWithPrefix(Path formName, String externalInstancePathPrefix) throws IOException {
        final List<String> errorMessages = new ArrayList<>();
        FormDef formDef = XFormUtils.getFormFromInputStream(new FileInputStream(formName.toString()), externalInstancePathPrefix,
            parser -> {
                parser.onWarning((message, xmlLocation) -> errorMessages.add(message));
                parser.onError(errorMessages::add);
            });
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
