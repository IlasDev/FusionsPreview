package com.gmail.ilasdeveloper.fusionspreview.data.models;

import java.util.ArrayList;

public class PokemonCollection {

    private String name;
    private ArrayList<String> types;
    private int[] stats;
    private int id;

    public PokemonCollection(ArrayList<String> types, int[] stats, String name, int id) {
        this.types = types;
        this.stats = stats;
        this.name = name;
        this.id = id + 1;
    }

    public ArrayList<String> getTypes() {
        return types;
    }

    public int[] getStats() {
        return stats;
    }

    public String getName() {
        return name;
    }

    public int getId() {
        return id;
    }
}
