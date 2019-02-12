package org.javarosa.core.reference;

import java.nio.file.Path;

public class ReferenceManagerTestUtils {
    /**
     * Returns a new ReferenceFactory that will derive (resolve) the given scheme to the given
     * path.
     */
    public static PrefixedRootFactory buildReferenceFactory(String scheme, final String path) {
        return new PrefixedRootFactory(new String[]{scheme + "/"}) {
            @Override
            protected Reference factory(String terminal, String URI) {
                return new ResourceReference(path + "/" + terminal);
            }
        };
    }

    /**
     * Sets up the ReferenceManager singleton to derive (resolve) the given schema to the
     * given path.
     * <p>
     * Please, be aware that this method resets the singleton ReferenceManager, which could
     * have unintended consequences for other classes using it during the same JVM session.
     * <p>
     * Use of this method is intended when only one scheme is to be derived. If your test
     * form uses more than one scheme, you will have to follow a more conventional setup of
     * having a reference factory for jr://file to a base path and some session translators
     * that derive any other scheme (e.g. jr://audio) to a jr://file path.
     */
    public static ReferenceManager setUpSimpleReferenceManager(String scheme, Path path) {
        ReferenceManager refManager = ReferenceManager.instance();
        refManager.reset();
        refManager.addReferenceFactory(buildReferenceFactory(
            scheme,
            path.toAbsolutePath().toString()
        ));
        return refManager;
    }
}
