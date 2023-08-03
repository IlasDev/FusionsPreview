package com.gmail.ilasdeveloper.fusionspreview.models;

public class Pokemon {
    private String name;
    private int[] stats;
    private String[] types;

    public Pokemon(String name, int[] stats) {
        this.name = name;
        this.stats = stats;
    }

    public Pokemon(String name, String[] types) {
        this.name = name;
        this.types = types;
    }

    public String getName() {
        return name;
    }

    public int[] getStats() {
        return stats;
    }

    public String[] getTypes() {
        return types;
    }
}