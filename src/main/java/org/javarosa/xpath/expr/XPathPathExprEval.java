/*
 * Copyright 2018 Nafundi
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

package org.javarosa.xpath.expr;

import org.javarosa.core.model.condition.EvaluationContext;
import org.javarosa.core.model.instance.DataInstance;
import org.javarosa.core.model.instance.TreeReference;
import org.javarosa.xpath.XPathException;
import org.javarosa.xpath.XPathMissingInstanceException;
import org.javarosa.xpath.XPathNodeset;

import java.util.List;

/** The eval operation of XPathPathExpr */
public class XPathPathExprEval {

    public XPathNodeset eval(TreeReference reference, EvaluationContext ec) {
        TreeReference ref = getContextualizedTreeReference(reference, ec);
        DataInstance dataInstance = getDataInstance(ec, ref);
        List<TreeReference> nodesetRefs = ec.expandReference(ref);
        removeIrrelevantNodesets(dataInstance, nodesetRefs);
        return new XPathNodeset(nodesetRefs, dataInstance, ec);
    }

    /** Removes irrelevant nodesets, to fix conditions based on non-relevant data */
    private void removeIrrelevantNodesets(DataInstance dataInstance, List<TreeReference> nodesetRefs) {
        for (int i = 0; i < nodesetRefs.size(); i++) {
            if (!dataInstance.resolveReference(nodesetRefs.get(i)).isRelevant()) {
                nodesetRefs.remove(i);
                i--;
            }
        }
    }

    private DataInstance getDataInstance(EvaluationContext ec, TreeReference ref) {
        final DataInstance dataInstance;

        if (refersToNonMainInstance(ref)) {
            final DataInstance nonMainInstance = ec.getInstance(ref.getInstanceName());
            if (nonMainInstance != null) {
                dataInstance = nonMainInstance;
            } else {
                throw new XPathMissingInstanceException(ref.getInstanceName(),
                    "Instance referenced by " + ref.toString(true) + " does not exist");
            }
        } else {
            //TODO: We should really stop passing 'dataInstance' around and start just getting the right instance from ec at a more central level
            dataInstance = ec.getMainInstance();

            if (dataInstance == null) {
                throw new XPathException("Cannot evaluate the reference [" + ref.toString(true) +
                    "] in the current evaluation context. No default instance has been declared!");
            }
        }
        // Regardless of the above, we want to ensure there is a definition
        if (dataInstance.getRoot() == null) {
            //This instance is _declared_, but doesn't actually have any data in it.
            throw new XPathMissingInstanceException(ref.getInstanceName(),
                "Instance referenced by " + ref.toString(true) + " has not been loaded");
        }

        return dataInstance;
    }

    private boolean refersToNonMainInstance(TreeReference ref) {
        return ref.getInstanceName() != null && ref.isAbsolute();
    }

    private TreeReference getContextualizedTreeReference(TreeReference genericRef, EvaluationContext ec) {
        // We don't necessarily know the model we want to be working with until we've contextualized the node
        TreeReference contextRef = genericRef.getContext() == TreeReference.CONTEXT_ORIGINAL ?
            ec.getOriginalContext() : ec.getContextRef();
        return genericRef.contextualize(contextRef);
    }
}
