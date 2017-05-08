package org.javarosa.core.model.instance.utils;

import org.javarosa.core.model.instance.AbstractTreeElement;
import org.javarosa.core.model.instance.DataInstance;
import org.javarosa.core.model.instance.InstanceInitializationFactory;
import org.javarosa.core.model.instance.TreeElement;
import org.kxml2.io.KXmlParser;
import org.kxml2.kdom.Document;
import org.xmlpull.v1.XmlPullParserException;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Objects;

public class ExternalDataInstance extends DataInstance {
    private final String srcLocation;

    public ExternalDataInstance(String srcLocation, String instanceid) {
        super(instanceid);
        setName(instanceid);
        this.srcLocation = srcLocation;
        TreeElement root;
        Document doc = new Document();
        try {
            URI uri = new URI(srcLocation);
            if (Objects.equals(uri.getScheme(), "jr") && Objects.equals(uri.getHost(), "file")) {
                KXmlParser parser = new KXmlParser();
                final String absolutePath = /* ToDo: find out how to get the actual location */
                        System.getProperty("user.dir") + "/resources" + uri.getPath();
                parser.setInput( new InputStreamReader(new FileInputStream(absolutePath), "UTF-8"));
                parser.setFeature(KXmlParser.FEATURE_PROCESS_NAMESPACES, true);
                doc.parse(parser);
            }
        } catch (XmlPullParserException | IOException | URISyntaxException e) {
            e.printStackTrace(); // ToDo: decide what errors to report and how to report them
        }
        //todo root = ...
    }

    @Override
    public AbstractTreeElement getBase() {
        return null; // ToDo
    }

    @Override
    public AbstractTreeElement getRoot() {
        return new TreeElement(srcLocation); // ToDo
    }

    @Override
    public void initialize(InstanceInitializationFactory initializer, String instanceId) {
        throw new RuntimeException("Not implemented");
    }
}
