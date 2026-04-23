package model;

import java.util.Map;

public class Coffee {
    private final String name;
    private final double price;
    private final Map<String, Integer> recipe; // ingredient name -> quantity

    public Coffee(String name, double price, Map<String, Integer> recipe) {
        this.name = name;
        this.price = price;
        this.recipe = recipe;
    }

    public String getName() { return name; }
    public double getPrice() { return price; }
    public Map<String, Integer> getRecipe() { return recipe; }
}

