package org.javarosa.xform.parse;

import org.javarosa.core.model.instance.CsvExternalInstance;
import org.javarosa.core.model.instance.TreeElement;
import org.javarosa.core.model.instance.XmlExternalInstance;
import org.javarosa.core.model.instance.geojson.GeoJsonExternalInstance;
import org.javarosa.core.reference.InvalidReferenceException;
import org.javarosa.core.reference.ReferenceManager;
import org.javarosa.xml.util.InvalidStructureException;
import org.javarosa.xml.util.UnfullfilledRequirementsException;
import org.jetbrains.annotations.NotNull;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.Arrays.asList;

public class ExternalInstanceParser {

    private List<ExternalDataInstanceProcessor> externalDataInstanceProcessors = new ArrayList<>();
    private List<FileInstanceParser> fileInstanceParsers = asList(
        new CsvExternalInstance(),
        new GeoJsonExternalInstance()
    );

    public TreeElement parse(ReferenceManager referenceManager, String instanceId, String instanceSrc) throws IOException, UnfullfilledRequirementsException, InvalidStructureException, XmlPullParserException, InvalidReferenceException {
        String path = getPath(referenceManager, instanceSrc);

        Optional<FileInstanceParser> fileParser = fileInstanceParsers.stream()
            .filter(fileInstanceParser -> fileInstanceParser.isSupported(instanceId, instanceSrc))
            .findFirst();

        TreeElement root;
        if (fileParser.isPresent()) {
            root = fileParser.get().parse(instanceId, path);
        } else {
            root = XmlExternalInstance.parse(instanceId, path);
        }

        for (ExternalDataInstanceProcessor processor : externalDataInstanceProcessors) {
            processor.processInstance(instanceId, root);
        }

        return root;
    }

    public void addProcessor(Processor processor) {
        externalDataInstanceProcessors.add((ExternalDataInstanceProcessor) processor);
    }

    /**
     * Adds {@link FileInstanceParser} before others. The last added {@link FileInstanceParser} will be checked
     * (via {@link FileInstanceParser#isSupported(String, String)}) first.
     */
    public void addFileInstanceParser(FileInstanceParser fileInstanceParser) {
        fileInstanceParsers = Stream.concat(
            Stream.of(fileInstanceParser),
            fileInstanceParsers.stream()
        ).collect(Collectors.toList());
    }

    /**
     * Returns the path of the URI at srcLocation.
     *
     * @param referenceManager
     * @param srcLocation      the value of the <code>src</code> attribute of the <code>instance</code> element
     */
    private static String getPath(ReferenceManager referenceManager, String srcLocation) throws InvalidReferenceException {
        String uri = referenceManager.deriveReference(srcLocation).getLocalURI();
        return uri.startsWith("//") /* todo why is this? */ ? uri.substring(1) : uri;
    }

    public interface Processor {

    }

    public interface ExternalDataInstanceProcessor extends ExternalInstanceParser.Processor {
        void processInstance(@NotNull String id, @NotNull TreeElement root);
    }

    public interface FileInstanceParser {
        TreeElement parse(String instanceId, String path) throws IOException;
        boolean isSupported(String instanceId, String instanceSrc);
    }
}
