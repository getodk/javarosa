package org.javarosa.core.reference;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import org.junit.Before;
import org.junit.Test;

public class ReferenceManagerTest {

    private static PrefixedRootFactory buildReferenceFactory(String scheme, final String path) {
        return new PrefixedRootFactory(new String[]{scheme + "/"}) {
            @Override
            protected Reference factory(String terminal, String URI) {
                return new ResourceReference(path + "/" + terminal);
            }
        };
    }

    private ReferenceManager refManager = ReferenceManager.instance();

    @Before
    public void setUp() {
        refManager.reset();
    }

    @Test
    public void derives_uris_according_to_a_reference_factory() throws InvalidReferenceException {
        refManager.addReferenceFactory(buildReferenceFactory("file", "/some/path"));
        assertThat(
            refManager.DeriveReference("jr://file/some-file.jpg").getLocalURI(),
            is("/some/path/some-file.jpg")
        );
    }

    @Test
    public void derives_uris_according_to_a_root_translator() throws InvalidReferenceException {
        refManager.addReferenceFactory(buildReferenceFactory("file", "/some/path"));
        refManager.addRootTranslator(new RootTranslator("jr://image/", "jr://file/forms/some-form-media/"));
        // This configuration will translate jr://image/... to jr://file/forms/some-form-media/... and then to /some/path/forms/some-form-media/...
        assertThat(
            refManager.DeriveReference("jr://image/some-file.jpg").getLocalURI(),
            is("/some/path/forms/some-form-media/some-file.jpg")
        );
    }

    @Test
    public void can_define_and_use_root_translators_during_a_session() throws InvalidReferenceException {
        refManager.addReferenceFactory(buildReferenceFactory("file", "/some/path"));
        refManager.addRootTranslator(new RootTranslator("jr://image/", "jr://file/forms/some-form-media/"));
        refManager.addSessionRootTranslator(new RootTranslator("jr://images/", "jr://image/"));
        assertThat(
            refManager.DeriveReference("jr://images/some-file.jpg").getLocalURI(),
            is("/some/path/forms/some-form-media/some-file.jpg")
        );
    }

    @Test(expected = InvalidReferenceException.class)
    public void session_root_translator_are_removed_after_cleaning_the_session() throws InvalidReferenceException {
        refManager.addReferenceFactory(buildReferenceFactory("file", "/some/path"));
        refManager.addRootTranslator(new RootTranslator("jr://image/", "jr://file/forms/some-form-media/"));
        refManager.addSessionRootTranslator(new RootTranslator("jr://images/", "jr://image/"));
        refManager.clearSession();
        refManager.DeriveReference("jr://images/some-file.jpg").getLocalURI();
    }

    @Test
    public void avoids_infinite_recursion() throws InvalidReferenceException {
        refManager.addReferenceFactory(buildReferenceFactory("file", "/some/path"));
        refManager.addSessionRootTranslator(new RootTranslator("jr://file/", "jr://file/forms/some-form-media/"));
        assertThat(
            refManager.DeriveReference("jr://file/some-file.xml").getLocalURI(),
            is("/some/path/forms/some-form-media/some-file.xml")
        );
    }
}
