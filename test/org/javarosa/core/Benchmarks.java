package org.javarosa.core;

import org.javarosa.core.model.FormDef;
import org.javarosa.core.model.instance.TreeElement;
import org.javarosa.core.test.FormParseInit;
import org.javarosa.form.api.FormEntryController;
import org.javarosa.xform.parse.IXFormParserFactory;
import org.javarosa.xform.parse.XFormParser;
import org.javarosa.xform.parse.XFormParserFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;

import static org.javarosa.test.utils.ResourcePathHelper.r;
import static org.junit.Assert.fail;


public class Benchmarks {

    public static InputStream getFileInputStream(String path){
        File initialFile = new File(path);
        try (InputStream targetStream = new FileInputStream(initialFile)) {
                return targetStream;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    private static IXFormParserFactory _factory = new XFormParserFactory();
    public static IXFormParserFactory setXFormParserFactory(IXFormParserFactory factory) {
        IXFormParserFactory oldFactory = _factory;
        _factory = factory;
        return oldFactory;
    }

    public static  IXFormParserFactory getXFormParserFactory(){
        return _factory;
    }


    public static Object benchMark2(){
        // Given
        FormParseInit formParseInit = new FormParseInit(r("populate-nodes-attributes.xml"));

        FormEntryController formEntryController = formParseInit.getFormEntryController();

        byte[] formInstanceAsBytes = null;
        try {
            formInstanceAsBytes =
                Files.readAllBytes(Paths.get(PathConst.getTestResourcePath().getAbsolutePath(),
                    "populate-nodes-attributes-instance.xml"));
        } catch (IOException e) {
            fail("There was a problem with reading the test data.\n" + e.getMessage());
        }
        TreeElement savedRoot = XFormParser.restoreDataModel(formInstanceAsBytes, null).getRoot();
        FormDef formDef = formEntryController.getModel().getForm();
        TreeElement dataRootNode = formDef.getInstance().getRoot().deepCopy(true);
        return dataRootNode;
    }

}
