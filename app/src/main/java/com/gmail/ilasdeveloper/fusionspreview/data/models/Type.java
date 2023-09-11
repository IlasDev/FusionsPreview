package com.gmail.ilasdeveloper.fusionspreview.data.models;

public class Type {

    private String name;
    private int amount;

    public Type(String name, int amount) {
        this.name = name;
        this.amount = amount;
    }

    public String getName() {
        return name;
    }

    public int getAmount() {
        return amount;
    }
}
