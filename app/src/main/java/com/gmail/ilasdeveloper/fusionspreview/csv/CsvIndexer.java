package com.gmail.ilasdeveloper.fusionspreview.csv;

import android.content.Context;

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

    private Map<String, String> index;

    private CsvIndexer(Context context, String csvUrl) {
        index = new HashMap<>();
        buildIndex(context, csvUrl);
    }

    public static CsvIndexer createInstance(Context context, String csvUrl) {
        instance = new CsvIndexer(context, csvUrl);
        return instance;
    }

    public static CsvIndexer getInstance() {
        return instance;
    }

    private void buildIndex(Context context, String csvUrl) {
        try {
            URL url = new URL(csvUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));

            CSVParser csvParser = CSVFormat.DEFAULT.parse(reader);
            List<CSVRecord> records = csvParser.getRecords();

            for (CSVRecord record : records) {
                String key = record.get(0); // n1.n2 value
                String value = record.get(1); // username
                index.put(key, value);
            }

            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String search(String fusion) {
        if (instance == null)
            return "Unknowwn";
        return index.get(fusion);
    }

    public String search(int head, int body) {
        if (instance == null)
            return "Unknowwn";
        return index.get(head + "." + body);
    }
}
