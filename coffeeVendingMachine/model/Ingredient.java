package model;

public class Ingredient {
    private final String name;
    private int quantity;

    public Ingredient(String name, int quantity) {
        this.name = name;
        this.quantity = quantity;
    }

    public String getName() { return name; }
    public synchronized int getQuantity() { return quantity; }

    public synchronized void updateQuantity(int amount) {
        this.quantity += amount;
    }

    public synchronized boolean isLow() {
        return quantity < 5;
    }
}

