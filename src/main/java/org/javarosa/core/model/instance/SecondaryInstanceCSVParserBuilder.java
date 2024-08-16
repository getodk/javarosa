package org.javarosa.core.model.instance;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.io.input.BOMInputStream;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;

public class SecondaryInstanceCSVParserBuilder {

    private String path;

    public SecondaryInstanceCSVParserBuilder path(String path) {
        this.path = path;
        return this;
    }

    public CSVParser build() throws IOException {
        final CSVFormat csvFormat = CSVFormat.DEFAULT
            .withDelimiter(getDelimiter(path))
            .withFirstRecordAsHeader();
        Reader reader = new InputStreamReader(new BOMInputStream(new FileInputStream(path)));
        return new CSVParser(reader, csvFormat);
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
