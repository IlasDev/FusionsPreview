package com.gmail.ilasdeveloper.fusionspreview.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class PokeAPI {
    private static final String BASE_URL = "https://pokeapi.co/api/v2/";
    private static final ExecutorService executor = Executors.newFixedThreadPool(10);
    public static final String[] STATS_NAMES = {"HP", "Attack", "Defense", "Sp. Atk", "Sp. Def", "Speed", "Total"};

    public static CompletableFuture<String> getPokemonAsync(String name) {
        String endpoint = "pokemon/" + name.toLowerCase();
        return sendRequestAsync(endpoint);
    }

    public static CompletableFuture<String> getAbilityAsync(int id) {
        String endpoint = "ability/" + id;
        return sendRequestAsync(endpoint);
    }

    public static CompletableFuture<String> getItemAsync(String name) {
        String endpoint = "item/" + name.toLowerCase();
        return sendRequestAsync(endpoint);
    }

    private static CompletableFuture<String> sendRequestAsync(String endpoint) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                URL url = new URL(BASE_URL + endpoint);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");

                int responseCode = connection.getResponseCode();
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                    String line;
                    StringBuilder response = new StringBuilder();
                    while ((line = reader.readLine()) != null) {
                        response.append(line);
                    }
                    reader.close();
                    return response.toString();
                } else {
                    throw new IOException("Request failed with HTTP code: " + responseCode);
                }
            } catch (IOException e) {
                throw new RuntimeException("Error sending request: " + e.getMessage(), e);
            }
        }, executor);
    }
}
