package org.javarosa.xform.parse;

import org.kxml2.kdom.Element;

public class ParseException extends Exception {

    public ParseException() {
        super();
    }

    public ParseException(String message) {
        super(message);
    }

    public ParseException (String msg, Element e) {
        super(msg + XFormParser.getVagueLocation(e));
    }

}
