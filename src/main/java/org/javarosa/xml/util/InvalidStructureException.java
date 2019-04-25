package org.javarosa.xml.util;

import org.kxml2.io.KXmlParser;

/**
 * Invalid Structure Exceptions are thrown when an invalid
 * definition is found while parsing XML defining CommCare
 * Models.
 *
 * @author ctsims
 */
public class InvalidStructureException extends Exception {
    /**
     * @param message A Message associated with the error.
     * @param parser  The parser in the position at which the error was detected.
     */
    public InvalidStructureException(String message, KXmlParser parser) {
        super("Invalid XML Structure(" + parser.getPositionDescription() + "): " + message);
    }

    /**
     * @param message A Message associated with the error.
     * @param parser  The parser in the position at which the error was detected.
     * @param file    The file being parsed
     */
    public InvalidStructureException(String message, String file, KXmlParser parser) {
        super("Invalid XML Structure in document " + file + "(" + parser.getPositionDescription() + "): " + message);
    }

    public InvalidStructureException(String message) {
        super(message);
    }

    public static InvalidStructureException readableInvalidStructureException(String message, KXmlParser parser) {
        String humanReadableMessage = message + buildParserMessage(parser);
        return new InvalidStructureException(humanReadableMessage);
    }

    private static String buildParserMessage(KXmlParser parser) {
        String prefix = parser.getPrefix();
        if (prefix != null) {
            return ". Source: <" + prefix + ":" + parser.getName() + "> tag in namespace: " + parser.getNamespace();
        } else {
            return ". Source: <" + parser.getName() + ">";
        }
    }
}
