package org.javarosa.xpath.expr;

import java.util.Collections;
import java.util.Date;
import org.javarosa.core.model.condition.EvaluationContext;
import org.javarosa.core.model.data.StringData;
import org.javarosa.core.model.instance.FormInstance;
import org.javarosa.core.model.instance.TreeElement;
import org.javarosa.core.model.instance.TreeReference;
import org.javarosa.xpath.XPathNodeset;
import org.junit.Ignore;
import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.not;
import static org.javarosa.xpath.expr.XPathFuncExpr.toLongHash;
import static org.javarosa.xpath.expr.XPathFuncExpr.toNumeric;

public class XPathFuncAsSomethingTest {

    @Test
    public void toLongHashHashesWell() {
        assertThat(toLongHash("Hello"), equalTo(1756278180214341157L));
        assertThat(toLongHash(""), equalTo(0L)); // the empty string would actually hash to -2039914840885289964L; but we've added this quirk for backward compatibility.
    }

    @Test
    public void toNumericHandlesBooleans() {
        assertThat(toNumeric(true), equalTo(1.0));
        assertThat(toNumeric(false), equalTo(0.0));
    }

    @Test
    public void toNumericHandlesStrings() {
        assertThat(toNumeric("  123  "), equalTo(123.0));
        assertThat(toNumeric("  123.0  "), equalTo(123.0));
        assertThat(toNumeric("  123.4  "), equalTo(123.4));
        assertThat(toNumeric("  123,4  "), equalTo(123.4));

        assertThat(toNumeric("0x12"), not(18.0));
        assertThat(toNumeric("0x12"), equalTo(Double.NaN));
    }

    @Test
    public void toNumericHandlesDates() {
        assertThat(toNumeric(new Date(86400 * 1000L)), equalTo(1.0));
    }

    @Ignore("Fails because JavaRosa does not currently use descendant text as the string-value of a nodeset (issue 862)")
    @Test
    public void toStringWithNodeset_returnsStringValueOfFirstNode() {
        TreeElement root = new TreeElement("root", 0);
        TreeElement item = new TreeElement("item", 0);
        TreeElement first = new TreeElement("first", 0);
        TreeElement second = new TreeElement("second", 0);

        first.setValue(new StringData("foo"));
        second.setValue(new StringData("bar"));

        item.addChild(first);
        item.addChild(second);
        root.addChild(item);

        FormInstance instance = new FormInstance(root);
        EvaluationContext ec = new EvaluationContext(instance);

        TreeReference itemRef = TreeReference.rootRef();
        itemRef.add("root", 0);
        itemRef.add("item", 0);

        XPathNodeset nodeset = new XPathNodeset(
            Collections.singletonList(itemRef),
            instance,
            ec
        );

        assertThat(XPathFuncExpr.toString(nodeset), equalTo("foobar"));
    }
}