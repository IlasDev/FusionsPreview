package com.gmail.ilasdeveloper.fusionspreview.data.models;

public class Ability {
    private String name;
    private boolean isHidden;

    public Ability(String name, boolean isHidden) {
        this.name = name;
        this.isHidden = isHidden;
    }

    public String getName() {
        return name;
    }

    public boolean isHidden() {
        return isHidden;
    }
}
