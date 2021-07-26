package org.javarosa.core.model.instance;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import org.javarosa.core.model.data.UncastData;

public class CsvExternalInstance {
    public static TreeElement parse(String instanceId, String path) throws IOException {
        TreeElement root = new TreeElement("root", 0);
        root.setInstanceName(instanceId);

        try (BufferedReader br = new BufferedReader(new FileReader(path))) {
            String csvLine = br.readLine();

            if (csvLine != null) {
                String[] fieldNames = csvLine.split(",");
                int multiplicity = 0;

                while ((csvLine = br.readLine()) != null) {
                    TreeElement item = new TreeElement("item", multiplicity);
                    String[] data = csvLine.split(",");
                    for (int i = 0; i < fieldNames.length; ++i) {
                        TreeElement field = new TreeElement(fieldNames[i], 0);
                        field.setValue(new UncastData(i < data.length ? data[i] : ""));

                        item.addChild(field);
                    }

                    root.addChild(item);
                    multiplicity++;
                }
            }
        }

        return root;
    }
}
