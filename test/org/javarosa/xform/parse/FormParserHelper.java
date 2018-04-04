package org.javarosa.xform.parse;

import org.javarosa.core.model.FormDef;

import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public final class FormParserHelper {

    private FormParserHelper() {
    }

    public static ParseResult parse(Path formName) throws IOException {
        XFormParser parser = new XFormParser(new FileReader(formName.toString()));
        final List<String> errorMessages = new ArrayList<>();
        parser.onWarning(new XFormParser.WarningCallback() {
            @Override
            public void accept(String message, String xmlLocation) {
                errorMessages.add(message);
            }
        });
        parser.onError(new XFormParser.ErrorCallback() {
            @Override
            public void accept(String message) {
                errorMessages.add(message);
            }
        });
        return new ParseResult(parser.parse(), errorMessages);
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
