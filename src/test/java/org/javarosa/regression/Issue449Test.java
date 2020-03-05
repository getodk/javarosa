/*
 * Copyright 2020 Nafundi
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.javarosa.regression;

import static org.javarosa.test.utils.ResourcePathHelper.r;

import java.nio.file.Path;
import org.javarosa.core.reference.ReferenceManagerTestUtils;
import org.javarosa.core.test.Scenario;
import org.junit.Ignore;
import org.junit.Test;

public class Issue449Test {

    /*
     * This test fails to run because the DAG detects a false cycle by self-reference
     * in the /data/aggregated field because references don't take into account the
     * instance where they're supposed to be applied to.
     */
    @Test
    @Ignore
    public void try_to_load_the_form() {
        Path formFile = r("issue_449.xml");
        ReferenceManagerTestUtils.setUpSimpleReferenceManager(formFile.getParent(), "file");
        Scenario.init(formFile);
    }
}
