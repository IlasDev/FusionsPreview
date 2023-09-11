package com.gmail.ilasdeveloper.fusionspreview.csv;


import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CsvIndexer {

    private static CsvIndexer instance;

    private final Map<String, String> index;

    private CsvIndexer(String csvUrl) {
        index = new HashMap<>();
        buildIndex(csvUrl);
    }

    public static CsvIndexer createInstance(String csvUrl) {
        instance = new CsvIndexer(csvUrl);
        return instance;
    }

    public static CsvIndexer getInstance() {
        return instance;
    }

    private void buildIndex(String csvUrl) {
        try {
            URL url = new URL(csvUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            BufferedReader reader =
                    new BufferedReader(new InputStreamReader(connection.getInputStream()));

            CSVParser csvParser = CSVFormat.DEFAULT.parse(reader);
            List<CSVRecord> records = csvParser.getRecords();

            for (CSVRecord record : records) {
                String key = record.get(0);
                String value = record.get(1);
                index.put(key, value);
            }

            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String search(String fusion) {
        if (instance == null) return "Unknowwn";
        return index.get(fusion);
    }

}
