package org.javarosa.xform.parse;

import org.javarosa.test.XFormsElement;
import org.javarosa.xpath.expr.XPathExpression;
import org.javarosa.xpath.expr.XPathPathExpr;
import org.javarosa.xpath.expr.XPathQName;
import org.javarosa.xpath.expr.XPathStep;
import org.jetbrains.annotations.NotNull;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.equalTo;
import static org.javarosa.test.BindBuilderXFormsElement.bind;
import static org.javarosa.test.XFormsElement.body;
import static org.javarosa.test.XFormsElement.head;
import static org.javarosa.test.XFormsElement.input;
import static org.javarosa.test.XFormsElement.mainInstance;
import static org.javarosa.test.XFormsElement.model;
import static org.javarosa.test.XFormsElement.t;

public class XPathProcessorTest {

    @Test
    public void processesXPathExpressions() throws Exception {
        XFormsElement form = XFormsElement.html(
            head(
                model(
                    mainInstance(
                        t("data id=\"form\"",
                            t("question")
                        )
                    ),
                    bind("/data/question").type("string")
                )
            ),
            body(
                input("/data/question")
            )
        );

        XFormParser parser = new XFormParser(new InputStreamReader(new ByteArrayInputStream(form.asXml().getBytes())));
        RecordingXPathProcessor processor = new RecordingXPathProcessor();
        parser.addProcessor(processor);
        parser.parse(null);

        assertThat(processor.processedExpressions, contains(
            new XPathPathExpr(XPathPathExpr.INIT_CONTEXT_ROOT, new XPathStep[]{
                new XPathStep(XPathStep.AXIS_CHILD, new XPathQName("data")),
                new XPathStep(XPathStep.AXIS_CHILD, new XPathQName("question"))
            }),
            new XPathPathExpr(XPathPathExpr.INIT_CONTEXT_ROOT, new XPathStep[]{
                new XPathStep(XPathStep.AXIS_CHILD, new XPathQName("data")),
                new XPathStep(XPathStep.AXIS_CHILD, new XPathQName("question"))
            })
        ));
    }

    @Test
    public void processorsAreNotRetainedBetweenParses() throws Exception {
        XFormsElement form = XFormsElement.html(
            head(
                model(
                    mainInstance(
                        t("data id=\"form\"",
                            t("question")
                        )
                    ),
                    bind("/data/question").type("string")
                )
            ),
            body(
                input("/data/question")
            )
        );

        XFormParser parser = new XFormParser(new InputStreamReader(new ByteArrayInputStream(form.asXml().getBytes())));
        RecordingXPathProcessor processor = new RecordingXPathProcessor();
        parser.addProcessor(processor);
        parser.parse(null);
        assertThat(processor.processedExpressions.size(), equalTo(2));

        XFormParser secondParser = new XFormParser(new InputStreamReader(new ByteArrayInputStream(form.asXml().getBytes())));
        secondParser.parse(null);
        assertThat(processor.processedExpressions.size(), equalTo(2));
    }

    private static class RecordingXPathProcessor implements XFormParser.XPathProcessor {

        public final List<XPathExpression> processedExpressions = new ArrayList<>();

        @Override
        public void processXPath(@NotNull XPathExpression xPathExpression) {
            processedExpressions.add(xPathExpression);
        }
    }
}
