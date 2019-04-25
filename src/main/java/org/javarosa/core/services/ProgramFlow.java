package org.javarosa.core.services;

import org.javarosa.core.log.FatalException;
import org.slf4j.LoggerFactory;

public final class ProgramFlow {
    private static final org.slf4j.Logger logger = LoggerFactory.getLogger(ProgramFlow.class);

    public static void die(String thread, Exception e) {
        //log exception
        logger.error("unhandled exception at top level", e);

        //crash
        final FatalException crashException = new FatalException("unhandled exception in " + thread, e);

        //depending on how the code was invoked, a straight 'throw' won't always reliably crash the app
        //throwing in a thread should work (at least on our nokias)
        new Thread() {
            public void run() {
                throw crashException;
            }
        }.start();

        //still do plain throw as a fallback
        try {
            Thread.sleep(3000);
        } catch (InterruptedException ie) {
        }
        throw crashException;
    }
}

