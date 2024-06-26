package org.javarosa.core.reference;

import java.io.File;

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
     */
    public static ReferenceManager setUpSimpleReferenceManager(File file, String... schemes) {
        ReferenceManager refManager = ReferenceManager.instance();
        refManager.reset();

        for (String scheme : schemes) {
            refManager.addReferenceFactory(buildReferenceFactory(
                scheme,
                file.getAbsolutePath()
            ));
        }
        return refManager;
    }
}
