package org.javarosa.core.model.instance;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.javarosa.core.model.data.UncastData;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;

public class CsvExternalInstance {
    public static TreeElement parse(String instanceId, String path) throws IOException {
        final TreeElement root = new TreeElement("root", 0);
        root.setInstanceName(instanceId);
        final CSVParser csvParser = CSVParser.parse(Paths.get(path),
            StandardCharsets.UTF_8, CSVFormat.DEFAULT.withFirstRecordAsHeader());
        final String[] fieldNames = csvParser.getHeaderMap().keySet().toArray(new String[0]);
        int multiplicity = 0;

        for (CSVRecord csvRecord : csvParser.getRecords()) {
            TreeElement item = new TreeElement("item", multiplicity);

            for (int i = 0; i < fieldNames.length; ++i) {
                TreeElement field = new TreeElement(fieldNames[i], 0);
                field.setValue(new UncastData(i < csvRecord.size() ? csvRecord.get(i) : ""));
                item.addChild(field);
            }

            root.addChild(item);
            multiplicity++;
        }

        return root;
    }
}
