package com.gmail.ilasdeveloper.fusionspreview.utils;


import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gmail.ilasdeveloper.fusionspreview.data.models.PokemonCollection;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class PokeAPI {
    public static final String[] STATS_NAMES = {
        "HP", "Attack", "Defense", "Sp. Atk", "Sp. Def", "Speed", "Total"
    };
    private static final String BASE_URL = "https://pokeapi.co/api/v2/";
    private static final ExecutorService executor = Executors.newFixedThreadPool(10);
    private static final ObjectMapper objectMapper = new ObjectMapper();

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
        return CompletableFuture.supplyAsync(
                () -> {
                    try {
                        URL url = new URL(BASE_URL + endpoint);
                        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                        connection.setRequestMethod("GET");

                        int responseCode = connection.getResponseCode();
                        if (responseCode == HttpURLConnection.HTTP_OK) {
                            BufferedReader reader =
                                    new BufferedReader(
                                            new InputStreamReader(connection.getInputStream()));
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
                },
                executor);
    }

    public static CompletableFuture<ArrayList<PokemonCollection>> fetchDataAsync() {
        return CompletableFuture.supplyAsync(() -> {
            try {
                URL url = new URL("https://cdn.jsdelivr.net/gh/IlasDev/InfiniteFusionData@main/pokemonData.min.json");
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");

                int responseCode = connection.getResponseCode();
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                    StringBuilder response = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        response.append(line);
                    }
                    reader.close();

                    ObjectMapper objectMapper = new ObjectMapper();
                    JsonNode root = objectMapper.readTree(response.toString());

                    ArrayList<PokemonCollection> pokemonList = new ArrayList<>();
                    Iterator<Map.Entry<String, JsonNode>> fieldsIterator = root.fields();
                    int i = 0;
                    while (fieldsIterator.hasNext()) {
                        Map.Entry<String, JsonNode> entry = fieldsIterator.next();
                        String pokemonName = entry.getKey();
                        JsonNode pokemonNode = entry.getValue();
                        JsonNode typesNode = pokemonNode.get("types");
                        ArrayList<String> types = new ArrayList<>();
                        for (JsonNode typeNode : typesNode) {
                            types.add(typeNode.asText());
                        }
                        JsonNode statsNode = pokemonNode.get("stats");
                        int[] stats = new int[7];
                        stats[0] = statsNode.get("hp").asInt();
                        stats[1] = statsNode.get("attack").asInt();
                        stats[2] = statsNode.get("defense").asInt();
                        stats[3] = statsNode.get("special-attack").asInt();
                        stats[4] = statsNode.get("special-defense").asInt();
                        stats[5] = statsNode.get("speed").asInt();
                        stats[6] = Arrays.stream(stats, 0, 6).sum();
                        PokemonCollection pokemonCollection = new PokemonCollection(types, stats, UtilsCollection.capitalizeFirstLetter(pokemonName), i);
                        pokemonList.add(pokemonCollection);
                        i++;
                    }

                    return pokemonList;
                } else {
                    throw new IOException("Request failed with HTTP code: " + responseCode);
                }
            } catch (IOException e) {
                throw new RuntimeException("Error sending request: " + e.getMessage(), e);
            }
        }, executor);
    }
}
