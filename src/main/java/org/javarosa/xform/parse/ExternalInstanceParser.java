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
import org.jetbrains.annotations.Nullable;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ExternalInstanceParser {

    private List<ExternalDataInstanceProcessor> externalDataInstanceProcessors = new ArrayList<>();
    private List<ExternalDataInstanceInterceptor> externalDataInstanceInterceptors = new ArrayList<>();

    public TreeElement parse(ReferenceManager referenceManager, String instanceId, String instanceSrc) throws IOException, UnfullfilledRequirementsException, InvalidStructureException, XmlPullParserException, InvalidReferenceException {
        TreeElement root = null;

        for (ExternalDataInstanceInterceptor interceptor : externalDataInstanceInterceptors) {
            root = interceptor.parse(instanceId, instanceSrc);
            if (root != null) {
                break;
            }
        }

        if (root == null) {
            String path = getPath(referenceManager, instanceSrc);
            root = instanceSrc.contains("file-csv") ? CsvExternalInstance.parse(instanceId, path)
                : instanceSrc.endsWith("geojson") ? GeoJsonExternalInstance.parse(instanceId, path)
                : XmlExternalInstance.parse(instanceId, path);
        }

        for (ExternalDataInstanceProcessor processor : externalDataInstanceProcessors) {
            processor.processInstance(instanceId, root);
        }

        return root;
    }

    public void addProcessor(Processor processor) {
        if (processor instanceof ExternalDataInstanceProcessor) {
            externalDataInstanceProcessors.add((ExternalDataInstanceProcessor) processor);
        } else {
            externalDataInstanceInterceptors.add((ExternalDataInstanceInterceptor) processor);
        }
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

    public interface ExternalDataInstanceInterceptor extends ExternalInstanceParser.Processor {
        @Nullable
        TreeElement parse(@NotNull String instanceId, @NotNull String instanceSrc);
    }

    public interface ExternalDataInstanceProcessor extends ExternalInstanceParser.Processor {
        void processInstance(@NotNull String id, @NotNull TreeElement root);
    }
}
