package com.gmail.ilasdeveloper.fusionspreview.data;

import com.gmail.ilasdeveloper.fusionspreview.data.models.Pokemon;

import java.util.ArrayList;

public class PokemonData {
    private static PokemonData instance;
    private final ArrayList<Pokemon> customStats;
    private final ArrayList<Pokemon> customTyping;

    private PokemonData() {
        customStats = new ArrayList<>();
        customStats.add(new Pokemon("Pidgeot", new int[]{83, 80, 75, 70, 70, 91}));
        customStats.add(new Pokemon("Pikachu", new int[]{35, 55, 30, 50, 40, 90}));
        customStats.add(new Pokemon("Raichu", new int[]{60, 90, 55, 90, 80, 100}));
        customStats.add(new Pokemon("Nidoqueen", new int[]{90, 82, 87, 75, 85, 76}));
        customStats.add(new Pokemon("Nidoking", new int[]{81, 92, 77, 85, 75, 85}));
        customStats.add(new Pokemon("Clefable", new int[]{95, 70, 73, 85, 90, 60}));
        customStats.add(new Pokemon("Wigglytuff", new int[]{140, 70, 45, 75, 50, 45}));
        customStats.add(new Pokemon("Vileplume", new int[]{75, 80, 85, 100, 90, 50}));
        customStats.add(new Pokemon("Poliwrath", new int[]{90, 85, 95, 70, 90, 70}));
        customStats.add(new Pokemon("Alakazam", new int[]{55, 50, 45, 135, 85, 120}));
        customStats.add(new Pokemon("Victreebel", new int[]{80, 105, 65, 100, 60, 70}));
        customStats.add(new Pokemon("Golem", new int[]{80, 110, 130, 55, 65, 45}));
        customStats.add(new Pokemon("Ampharos", new int[]{90, 75, 75, 115, 90, 55}));
        customStats.add(new Pokemon("Bellossom", new int[]{75, 80, 85, 90, 100, 50}));
        customStats.add(new Pokemon("Azumarill", new int[]{100, 50, 80, 50, 80, 50}));
        customStats.add(new Pokemon("Jumpluff", new int[]{75, 55, 70, 55, 85, 110}));
        customStats.add(new Pokemon("Roserade", new int[]{60, 70, 55, 125, 105, 90}));
        customStats.add(new Pokemon("Krookodile", new int[]{95, 117, 70, 65, 70, 92}));
        customStats.add(new Pokemon("Magcargo", new int[]{50, 50, 120, 80, 80, 30}));
        customStats.add(new Pokemon("Arbok", new int[]{60, 85, 69, 65, 79, 80}));
        customStats.add(new Pokemon("Ariados", new int[]{70, 90, 70, 60, 60, 40}));
        customStats.add(new Pokemon("Mantine", new int[]{65, 40, 70, 80, 140, 70}));
        customStats.add(new Pokemon("Electrode", new int[]{60, 50, 70, 80, 80, 140}));
        customStats.add(new Pokemon("Dodrio", new int[]{60, 110, 70, 60, 60, 100}));
        customStats.add(new Pokemon("Noctowl", new int[]{100, 50, 50, 76, 96, 70}));
        customStats.add(new Pokemon("Qwilfish", new int[]{65, 95, 75, 55, 55, 85}));
        customStats.add(new Pokemon("Dugtrio", new int[]{35, 80, 50, 50, 70, 120}));
        customStats.add(new Pokemon("Farfetchd", new int[]{52, 65, 55, 58, 62, 60}));
        customStats.add(new Pokemon("Corsola", new int[]{55, 55, 85, 65, 85, 35}));
        customStats.add(new Pokemon("Exeggutor", new int[]{95, 95, 85, 125, 65, 55}));

        customTyping = new ArrayList<>();
        customTyping.add(new Pokemon("Magnemite", new String[]{"steel", "electric"}));
        customTyping.add(new Pokemon("Magneton", new String[]{"steel", "electric"}));
        customTyping.add(new Pokemon("Dewgong", new String[]{"ice", "water"}));
        customTyping.add(new Pokemon("Omanyte", new String[]{"water", "rock"}));
        customTyping.add(new Pokemon("Omastar", new String[]{"water", "rock"}));
        customTyping.add(new Pokemon("Scizor", new String[]{"steel", "bug"}));
        customTyping.add(new Pokemon("Magnezone", new String[]{"steel", "electric"}));
        customTyping.add(new Pokemon("Empoleon", new String[]{"steel", "water"}));
        customTyping.add(new Pokemon("Spiritomb", new String[]{"dark", "ghost"}));
        customTyping.add(new Pokemon("Ferrothorn", new String[]{"steel", "grass"}));
        customTyping.add(new Pokemon("Celebi", new String[]{"grass", "psychic"}));
        customTyping.add(new Pokemon("Gastly", new String[]{"ghost"}));
        customTyping.add(new Pokemon("Haunter", new String[]{"ghost"}));
        customTyping.add(new Pokemon("Gengar", new String[]{"ghost"}));
        customTyping.add(new Pokemon("Onix", new String[]{"rock"}));
        customTyping.add(new Pokemon("Scyther", new String[]{"bug"}));
        customTyping.add(new Pokemon("Gyarados", new String[]{"water"}));
        customTyping.add(new Pokemon("Articuno", new String[]{"ice"}));
        customTyping.add(new Pokemon("Zapdos", new String[]{"electric"}));
        customTyping.add(new Pokemon("Moltres", new String[]{"fire"}));
        customTyping.add(new Pokemon("Dragonite", new String[]{"dragon"}));
        customTyping.add(new Pokemon("Steelix", new String[]{"steel"}));
        customTyping.add(new Pokemon("Bulbasaur", new String[]{"grass"}));
        customTyping.add(new Pokemon("Ivysaur", new String[]{"grass"}));
        customTyping.add(new Pokemon("Venusaur", new String[]{"grass"}));
        customTyping.add(new Pokemon("Charizard", new String[]{"fire"}));
        customTyping.add(new Pokemon("Geodude", new String[]{"rock"}));
        customTyping.add(new Pokemon("Graveler", new String[]{"rock"}));
        customTyping.add(new Pokemon("Golem", new String[]{"rock"}));
    }

    public static synchronized PokemonData getInstance() {
        if (instance == null) {
            instance = new PokemonData();
        }
        return instance;
    }

    public ArrayList<Pokemon> getCustomStats() {
        return customStats;
    }

    public ArrayList<Pokemon> getCustomTyping() {
        return customTyping;
    }
}
