package org.javarosa.xform.parse;

import org.javarosa.core.model.FormDef;
import org.javarosa.core.test.FormParseInit;
import org.junit.Test;

import java.nio.file.Path;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.javarosa.core.reference.ReferenceManagerTestUtils.setUpSimpleReferenceManager;
import static org.javarosa.test.utils.ResourcePathHelper.r;

public class ExternalSecondaryInstanceTest {
    @Test
    public void externalInstanceDeclaration_ShouldBeIgnored_WhenNotReferenced() {
        Path formPath = r("unused-secondary-instance.xml");
        setUpSimpleReferenceManager("file-csv", formPath.getParent());
        FormParseInit fpi = new FormParseInit(formPath);
        FormDef formDef = fpi.getFormDef();

        assertThat(formDef.getNonMainInstance("fruits").getRoot().hasChildren(), is(false));
    }
}
