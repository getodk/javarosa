package org.javarosa.benchmarks;

import org.javarosa.xml.KXmlElementParser;
import org.kxml2.io.KXmlParser;
import org.kxml2.kdom.Document;
import org.kxml2.kdom.Element;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Level;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.infra.Blackhole;
import org.xmlpull.v1.XmlPullParserException;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import static org.javarosa.benchmarks.BenchmarkUtils.dryRun;

public class KXmlElementParserBenchmark {
    public static void main(String[] args) {
        dryRun(KXmlElementParserBenchmark.class);
    }

    @State(Scope.Thread)
    public static class ElementParserState {
        String xFormMinifiedInternalSecondaryInstances;
        String xFormInternalSecondaryInstances;
        String xFormExternalSecondayInstances;
        String lgasInstance;
        String wardsInstance;
        @Setup(Level.Trial)
        public void initialize() {
            xFormMinifiedInternalSecondaryInstances = BenchmarkUtils.getMinifiedNigeriaWardsXMLWithInternal2ndryInstance().toString();
            xFormInternalSecondaryInstances = BenchmarkUtils.getNigeriaWardsXMLWithInternal2ndryInstance().toString();
            xFormExternalSecondayInstances = BenchmarkUtils.getNigeriaWardsXMLWithExternal2ndryInstance().toString();
            lgasInstance = BenchmarkUtils.getLGAsExternalInstance().toString();
            wardsInstance = BenchmarkUtils.getWardsExternalInstance().toString();
        }
    }

    @Benchmark
    public void benchmarkParseMinifiedInternalInstanceXForm(ElementParserState state, Blackhole bh) throws IOException, XmlPullParserException  {
        Element documentRootElement = parse( state.xFormMinifiedInternalSecondaryInstances);
        bh.consume(documentRootElement);
    }

    @Benchmark
    public void benchmarkParseExternalInstanceXFormOnly(ElementParserState state, Blackhole bh) throws IOException, XmlPullParserException  {
        Element documentRootElement = parse(state.xFormExternalSecondayInstances);
        bh.consume(documentRootElement);
    }

    @Benchmark
    public void benchmarkParseInternalInstanceXForm(ElementParserState state, Blackhole bh) throws IOException, XmlPullParserException  {
        Element Element = parse(state.xFormInternalSecondaryInstances);
        bh.consume(Element);
    }

    @Benchmark
    public void benchmarkParseExternalInstanceLGAs(ElementParserState state, Blackhole bh) throws IOException, XmlPullParserException  {
        Element Element = parse(state.lgasInstance);
        bh.consume(Element);
    }

    @Benchmark
    public void benchmarkParseExternalInstanceWards(ElementParserState state, Blackhole bh) throws IOException, XmlPullParserException  {
        Element Element = parse(state.wardsInstance);
        bh.consume(Element);
    }

    @Benchmark
    public void benchmarkParseExternalInstanceXFormWithInstanceFiles(ElementParserState state, Blackhole bh) throws IOException, XmlPullParserException  {
        Element xFormElement = parse(state.xFormExternalSecondayInstances);
        Element lgaInstanceElement = parse(state.lgasInstance);
        Element wardInstanceElement = parse(state.wardsInstance);
        bh.consume(xFormElement);
        bh.consume(lgaInstanceElement);
        bh.consume(wardInstanceElement);
    }

    public static Element parse(String path) throws IOException, XmlPullParserException {
        InputStream inputStream = new FileInputStream(path);
        KXmlParser xmlParser = KXmlElementParser.instantiateParser(inputStream);
        KXmlElementParser KXmlElementParser = new KXmlElementParser(xmlParser);
        Document document = KXmlElementParser.parseDoc();
        return document.getRootElement();
    }
}
