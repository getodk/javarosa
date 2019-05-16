package org.javarosa.core.model.instance;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import org.javarosa.core.model.data.UncastData;

public class CsvExternalInstance {
    public static TreeElement parse(String instanceId, String path) throws IOException {
        TreeElement root = new TreeElement("root", 0);
        root.setInstanceName(instanceId);
        BufferedReader br = new BufferedReader(new FileReader(path));
        String csvLine = br.readLine();

        if (csvLine != null) {
            String[] fieldNames = csvLine.split(",");

            while ((csvLine = br.readLine()) != null) {
                TreeElement item = new TreeElement("item", 0);
                String[] data = csvLine.split(",");
                for (int i = 0; i < fieldNames.length; ++i) {
                    TreeElement field = new TreeElement(fieldNames[i], 0);
                    field.setValue(new UncastData(data[i]));
                    item.addChild(field);
                }

                root.addChild(item);
            }
        }
        return root;
    }
}
