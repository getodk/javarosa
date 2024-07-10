package org.javarosa.plugins;

import kotlin.Pair;
import org.javarosa.core.model.SelectChoice;
import org.javarosa.core.model.data.StringData;
import org.javarosa.core.model.instance.DataInstance;
import org.javarosa.core.model.instance.TreeElement;
import org.javarosa.test.Scenario;
import org.javarosa.test.TempFileUtils;
import org.javarosa.xform.parse.ExternalInstanceParser;
import org.javarosa.xform.parse.ExternalInstanceParserFactory;
import org.javarosa.xform.parse.XFormParser;
import org.javarosa.xform.util.XFormUtils;
import org.jetbrains.annotations.NotNull;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;

import static java.util.Arrays.asList;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.javarosa.core.reference.ReferenceManagerTestUtils.setUpSimpleReferenceManager;
import static org.javarosa.test.BindBuilderXFormsElement.bind;
import static org.javarosa.test.XFormsElement.body;
import static org.javarosa.test.XFormsElement.head;
import static org.javarosa.test.XFormsElement.html;
import static org.javarosa.test.XFormsElement.mainInstance;
import static org.javarosa.test.XFormsElement.model;
import static org.javarosa.test.XFormsElement.select1Dynamic;
import static org.javarosa.test.XFormsElement.t;
import static org.javarosa.test.XFormsElement.title;

public class InstancePluginTest {

    private final SwitchableExternalInstanceParserFactory externalInstanceParserFactory = new SwitchableExternalInstanceParserFactory();

    @Before
    public void setup() {
        XFormUtils.setExternalInstanceParserFactory(externalInstanceParserFactory);
    }

    @After
    public void teardown() {
        XFormUtils.setExternalInstanceParserFactory(ExternalInstanceParser::new);
    }

    @Test
    public void supportsPartialElements() throws IOException, XFormParser.ParseException {
        externalInstanceParserFactory.setFileInstanceParser(new FakeFileInstanceParser(asList(
            new Pair<>("0", "Item 0"),
            new Pair<>("1", "Item 1")
        ), true));

        File tempFile = TempFileUtils.createTempFile("fake-instance", "fake");
        setUpSimpleReferenceManager(tempFile, "file-csv", "file");

        Scenario scenario = Scenario.init("Fake instance form", html(
                head(
                    title("Fake instance form"),
                    model(
                        mainInstance(
                            t("data id=\"fake-instance-form\"",
                                t("question")
                            )
                        ),
                        t("instance id=\"fake-instance\" src=\"jr://file-csv/fake-instance.fake\""),
                        bind("/data/question").type("string")
                    )
                ),
                body(
                    select1Dynamic("/data/question", "instance('fake-instance')/root/item")
                )
            )
        );

        HashMap<String, DataInstance> instances = scenario.getFormDef().getFormInstances();
        DataInstance fakeInstance = instances.get("fake-instance");
        assertThat(fakeInstance.getRoot().getNumChildren(), equalTo(2));

        TreeElement firstItem = (TreeElement) fakeInstance.getRoot().getChild("item", 0);
        assertThat(firstItem.isPartial(), equalTo(true));
        assertThat(firstItem.getNumChildren(), equalTo(2));
        assertThat(firstItem.getChildAt(0).getName(), equalTo("value"));
        assertThat(firstItem.getChildAt(0).getValue(), equalTo(null));
        assertThat(firstItem.getChildAt(1).getName(), equalTo("label"));
        assertThat(firstItem.getChildAt(1).getValue(), equalTo(null));

        List<SelectChoice> selectChoices = scenario.choicesOf("/data/question");
        assertThat(selectChoices.size(), equalTo(2));

        assertThat(selectChoices.get(0).getValue(), equalTo("0"));
        firstItem = (TreeElement) fakeInstance.getRoot().getChild("item", 0);
        assertThat(firstItem.isPartial(), equalTo(false));
        assertThat(firstItem.getNumChildren(), equalTo(2));
        assertThat(firstItem.getChildAt(0).getName(), equalTo("value"));
        assertThat(firstItem.getChildAt(0).getValue(), equalTo(new StringData("0")));
        assertThat(firstItem.getChildAt(1).getName(), equalTo("label"));
        assertThat(firstItem.getChildAt(1).getValue(), equalTo(new StringData("Item 0")));
    }

    @Test
    public void supportsReplacingPartialElements() throws IOException, XFormParser.ParseException {
        externalInstanceParserFactory.setFileInstanceParser(new FakeFileInstanceParser(asList(
            new Pair<>("0", "Item 0"),
            new Pair<>("1", "Item 1")
        ), true));

        File tempFile = TempFileUtils.createTempFile("fake-instance", "fake");
        setUpSimpleReferenceManager(tempFile, "file-csv", "file");

        Scenario scenario = Scenario.init("Fake instance form", html(
                head(
                    title("Fake instance form"),
                    model(
                        mainInstance(
                            t("data id=\"fake-instance-form\"",
                                t("question")
                            )
                        ),
                        t("instance id=\"fake-instance\" src=\"jr://file-csv/fake-instance.fake\""),
                        bind("/data/question").type("string")
                    )
                ),
                body(
                    select1Dynamic("/data/question", "instance('fake-instance')/root/item")
                )
            )
        );

        HashMap<String, DataInstance> instances = scenario.getFormDef().getFormInstances();
        DataInstance fakeInstance = instances.get("fake-instance");

        TreeElement item = new TreeElement("item", 0);
        TreeElement value = new TreeElement("value");
        TreeElement label = new TreeElement("label");
        value.setValue(new StringData("0"));
        label.setValue(new StringData("Item 0"));
        item.addChild(value);
        item.addChild(label);
        fakeInstance.replacePartialElements(asList(item));

        TreeElement firstItem = (TreeElement) fakeInstance.getRoot().getChild("item", 0);
        assertThat(firstItem.isPartial(), equalTo(false));
        assertThat(firstItem.getNumChildren(), equalTo(2));
        assertThat(firstItem.getChildAt(0).getName(), equalTo("value"));
        assertThat(firstItem.getChildAt(0).getValue(), equalTo(new StringData("0")));
        assertThat(firstItem.getChildAt(1).getName(), equalTo("label"));
        assertThat(firstItem.getChildAt(1).getValue(), equalTo(new StringData("Item 0")));
        TreeElement secondItem = (TreeElement) fakeInstance.getRoot().getChild("item", 1);
        assertThat(secondItem.isPartial(), equalTo(true));
        assertThat(secondItem.getNumChildren(), equalTo(2));
        assertThat(secondItem.getChildAt(0).getName(), equalTo("value"));
        assertThat(secondItem.getChildAt(0).getValue(), equalTo(null));
        assertThat(secondItem.getChildAt(1).getName(), equalTo("label"));
        assertThat(secondItem.getChildAt(1).getValue(), equalTo(null));
    }

    @Test
    public void replacePartialElements_DoesNotOverrideNonPartialElements() throws IOException, XFormParser.ParseException {
        externalInstanceParserFactory.setFileInstanceParser(new FakeFileInstanceParser(asList(
            new Pair<>("0", "Item 0")
        ), false));

        File tempFile = TempFileUtils.createTempFile("fake-instance", "fake");
        setUpSimpleReferenceManager(tempFile, "file-csv", "file");

        Scenario scenario = Scenario.init("Fake instance form", html(
                head(
                    title("Fake instance form"),
                    model(
                        mainInstance(
                            t("data id=\"fake-instance-form\"",
                                t("question")
                            )
                        ),
                        t("instance id=\"fake-instance\" src=\"jr://file-csv/fake-instance.fake\""),
                        bind("/data/question").type("string")
                    )
                ),
                body(
                    select1Dynamic("/data/question", "instance('fake-instance')/root/item")
                )
            )
        );

        HashMap<String, DataInstance> instances = scenario.getFormDef().getFormInstances();
        DataInstance fakeInstance = instances.get("fake-instance");

        TreeElement item = new TreeElement("item", 0);
        TreeElement value = new TreeElement("value");
        TreeElement label = new TreeElement("label");
        value.setValue(new StringData("1"));
        label.setValue(new StringData("Item 1"));
        item.addChild(value);
        item.addChild(label);
        fakeInstance.replacePartialElements(asList(item));

        TreeElement firstItem = (TreeElement) fakeInstance.getRoot().getChild("item", 0);
        assertThat(firstItem.isPartial(), equalTo(false));
        assertThat(firstItem.getNumChildren(), equalTo(2));
        assertThat(firstItem.getChildAt(0).getName(), equalTo("value"));
        assertThat(firstItem.getChildAt(0).getValue(), equalTo(new StringData("0")));
        assertThat(firstItem.getChildAt(1).getName(), equalTo("label"));
        assertThat(firstItem.getChildAt(1).getValue(), equalTo(new StringData("Item 0")));
    }

    private static class SwitchableExternalInstanceParserFactory implements ExternalInstanceParserFactory {
        private ExternalInstanceParser.FileInstanceParser fileInstanceParser;

        @Override
        public ExternalInstanceParser getExternalInstanceParser() {
            ExternalInstanceParser externalInstanceParser = new ExternalInstanceParser();
            externalInstanceParser.addFileInstanceParser(fileInstanceParser);
            return externalInstanceParser;
        }

        public void setFileInstanceParser(ExternalInstanceParser.FileInstanceParser fileInstanceParser) {
            this.fileInstanceParser = fileInstanceParser;
        }
    }

    public static class FakeFileInstanceParser implements ExternalInstanceParser.FileInstanceParser {

        private final List<Pair<String, String>> items;
        private final boolean partialParse;

        public FakeFileInstanceParser(List<Pair<String, String>> items, boolean partialParse) {
            this.items = items;
            this.partialParse = partialParse;
        }

        @Override
        public TreeElement parse(@NotNull String instanceId, @NotNull String path) throws IOException {
            return parse(instanceId, path, false);
        }

        @Override
        public TreeElement parse(@NotNull String instanceId, @NotNull String path, boolean partial) throws IOException {
            boolean isPartial = partialParse && partial;
            TreeElement root = new TreeElement("root", 0);

            for (int i = 0; i < items.size(); i++) {
                TreeElement value = new TreeElement("value");
                TreeElement label = new TreeElement("label");

                if (!isPartial) {
                    value.setValue(new StringData(items.get(i).getFirst()));
                    label.setValue(new StringData(items.get(i).getSecond()));
                }

                TreeElement item = new TreeElement("item", i, isPartial);
                item.addChild(value);
                item.addChild(label);

                root.addChild(item);
            }

            return root;
        }

        @Override
        public boolean isSupported(String instanceId, String instanceSrc) {
            return instanceSrc.endsWith(".fake");
        }
    }
}
