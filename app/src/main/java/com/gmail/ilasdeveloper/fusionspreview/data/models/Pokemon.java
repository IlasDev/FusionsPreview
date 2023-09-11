package com.gmail.ilasdeveloper.fusionspreview.data.models;

import java.util.ArrayList;
import java.util.List;

public class Pokemon {
    private String name;
    private int[] stats;
    private String[] types;
    private ArrayList<Ability> abilities;

    public Pokemon(String name, int[] stats) {
        this.name = name;
        this.stats = stats;
    }

    public Pokemon(String name, String[] types) {
        this.name = name;
        this.types = types;
    }

    public Pokemon(String name, List<Ability> abilities) {
        this.name = name;
        this.abilities = new ArrayList<>(abilities);
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

    public ArrayList<Ability> getAbilities() {
        return abilities;
    }
}
