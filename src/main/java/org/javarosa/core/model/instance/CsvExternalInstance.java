package org.javarosa.core.model.instance;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.javarosa.core.model.data.UncastData;

public class CsvExternalInstance {
    public static TreeElement parse(String instanceId, String path) throws IOException {
        final TreeElement root = new TreeElement("root", 0);
        root.setInstanceName(instanceId);

        final CSVFormat csvFormat = CSVFormat.DEFAULT
            .withDelimiter(getDelimiter(path))
            .withFirstRecordAsHeader();
        final CSVParser csvParser = CSVParser.parse(new File(path), StandardCharsets.UTF_8, csvFormat);
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

    private static char getDelimiter(String path) throws IOException {
        char delimiter = ',';
        try (BufferedReader reader = new BufferedReader(new FileReader(path))) {
            String header = reader.readLine();

            if (header.contains(";")) {
                delimiter = ';';
            }
        }
        return delimiter;
    }
}
