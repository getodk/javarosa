package org.javarosa.core.model.instance;

import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.javarosa.core.model.data.UncastData;
import org.javarosa.xform.parse.ExternalInstanceParser;
import org.jetbrains.annotations.NotNull;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class CsvExternalInstance implements ExternalInstanceParser.FileInstanceParser {

    public TreeElement parse(@NotNull String instanceId, @NotNull String path) throws IOException {
        final TreeElement root = new TreeElement("root", 0);
        root.setInstanceName(instanceId);

        try (
            final CSVParser csvParser = new SecondaryInstanceCSVParserBuilder()
                .path(path)
                .build()
        ) {
            final String[] fieldNames = csvParser.getHeaderMap().keySet().toArray(new String[0]);
            int multiplicity = 0;

            for (CSVRecord csvRecord : csvParser) {
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

    @Override
    public boolean isSupported(@NotNull String instanceId, @NotNull String instanceSrc) {
        return instanceSrc.contains("file-csv");
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
