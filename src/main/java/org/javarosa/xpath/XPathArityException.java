package org.javarosa.xpath;

/**
 * An exception detailing a function call that was provided the incorrect
 * number of arguments.
 *
 * Created by wpride1 on 3/28/15.
 */
public class XPathArityException extends XPathException {
    private int expectedArity;
    private int providedArity;
    private String funcName;
    private String errorMessage;

    public XPathArityException() {
    }

    /**
     * An exception detailing a function call that was provided the incorrect
     * number of arguments.
     *
     * @param funcName      name of function that was called with incorrect number
     *                      of arguments
     * @param expectedArity number of arguments expected for this function call
     * @param providedArity number of arguments provided for this function call
     */
    public XPathArityException(String funcName, int expectedArity, int providedArity) {
        super("The " + funcName +
                " function was provided the incorrect number of arguments:" + providedArity +
                ". It expected " + expectedArity + " arguments.");

        this.errorMessage = "The " + funcName +
                " function was provided the incorrect number of arguments:" + providedArity +
                ". It expected " + expectedArity + " arguments.";
        this.expectedArity = expectedArity;
        this.providedArity = providedArity;
        this.funcName = funcName;
    }

    /**
     * An exception detailing a function call that was provided the incorrect
     * number of arguments.
     *
     * @param funcName             name of function that was called with incorrect number
     *                             of arguments
     * @param expectedArityMessage message describing the expected arity logic
     *                             of the function in question
     * @param providedArity        number of arguments provided for this function call
     */
    public XPathArityException(String funcName, String expectedArityMessage, int providedArity) {
        super("The " + funcName +
                " function was provided the incorrect number of arguments:" + providedArity +
                ". It expected " + expectedArityMessage + ".");

        this.errorMessage = "The " + funcName +
                " function was provided the incorrect number of arguments:" + providedArity +
                ". It expected " + expectedArityMessage + ".";
        this.expectedArity = -1;
        this.providedArity = providedArity;
        this.funcName = funcName;
    }
}