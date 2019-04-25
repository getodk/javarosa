package org.javarosa.core.reference;

/**
 * A ResourceReferenceFactory is a Raw Reference Accessor
 * which provides a factory for references of the form
 * <pre>jr://resource/</pre>.
 * <p>
 * TODO: Configure this factory to also work for raw resource
 * accessors like "/something".
 *
 * @author ctsims
 */
public class ResourceReferenceFactory extends PrefixedRootFactory {

    public ResourceReferenceFactory() {
        super(new String[]{"resource"});
    }

    @Override
    protected Reference factory(String terminal, String URI) {
        return new ResourceReference(terminal);
    }
}
