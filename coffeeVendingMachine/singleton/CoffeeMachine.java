package singleton;

import model.*;

import java.util.HashMap;
import java.util.Map;

public class CoffeeMachine {

    private static CoffeeMachine instance;

    private final Map<String, Coffee> menu;
    private final Map<String, Ingredient> ingredients;

    private CoffeeMachine() {
        menu = new HashMap<>();
        ingredients = new HashMap<>();
        initializeIngredients();
        initializeMenu();
    }

    public static synchronized CoffeeMachine getInstance() {
        if (instance == null) {
            instance = new CoffeeMachine();
        }
        return instance;
    }

    private void initializeIngredients() {
        ingredients.put("espresso",  new Ingredient("espresso",  20));
        ingredients.put("milk",      new Ingredient("milk",      20));
        ingredients.put("foam",      new Ingredient("foam",      20));
        ingredients.put("water",     new Ingredient("water",     20));
    }

    private void initializeMenu() {
        // Espresso: 1 shot espresso + 1 water
        Map<String, Integer> espressoRecipe = new HashMap<>();
        espressoRecipe.put("espresso", 2);
        espressoRecipe.put("water",    1);
        menu.put("espresso", new Coffee("Espresso", 2.50, espressoRecipe));

        // Cappuccino: 1 espresso + 1 milk + 1 foam
        Map<String, Integer> cappuccinoRecipe = new HashMap<>();
        cappuccinoRecipe.put("espresso", 2);
        cappuccinoRecipe.put("milk",     2);
        cappuccinoRecipe.put("foam",     1);
        menu.put("cappuccino", new Coffee("Cappuccino", 3.50, cappuccinoRecipe));

        // Latte: 1 espresso + 3 milk
        Map<String, Integer> latteRecipe = new HashMap<>();
        latteRecipe.put("espresso", 2);
        latteRecipe.put("milk",     3);
        menu.put("latte", new Coffee("Latte", 4.00, latteRecipe));
    }

    public void displayMenu() {
        System.out.println("\n===== Coffee Vending Machine Menu =====");
        for (Coffee coffee : menu.values()) {
            System.out.printf("  %-15s $%.2f%n", coffee.getName(), coffee.getPrice());
        }
        System.out.println("=======================================\n");
    }

    public synchronized void selectCoffee(String coffeeType, Payment payment) {
        Coffee coffee = menu.get(coffeeType.toLowerCase());

        if (coffee == null) {
            System.out.println("[ERROR] Coffee type '" + coffeeType + "' not found in menu.");
            return;
        }

        if (payment.getAmount() < coffee.getPrice()) {
            System.out.printf("[ERROR] Insufficient payment. Required: $%.2f, Paid: $%.2f%n",
                    coffee.getPrice(), payment.getAmount());
            return;
        }

        if (!hasEnoughIngredients(coffee)) {
            System.out.println("[ERROR] Insufficient ingredients to make " + coffee.getName() + ".");
            return;
        }

        dispenseCoffee(coffee, payment);
    }

    private void dispenseCoffee(Coffee coffee, Payment payment) {
        updateIngredients(coffee);
        double change = payment.getAmount() - coffee.getPrice();
        System.out.printf("[INFO] Dispensing %s...%n", coffee.getName());
        if (change > 0) {
            System.out.printf("[INFO] Change returned: $%.2f%n", change);
        }
        checkInventory();
    }

    private boolean hasEnoughIngredients(Coffee coffee) {
        for (Map.Entry<String, Integer> entry : coffee.getRecipe().entrySet()) {
            Ingredient ingredient = ingredients.get(entry.getKey());
            if (ingredient == null || ingredient.getQuantity() < entry.getValue()) {
                return false;
            }
        }
        return true;
    }

    private void updateIngredients(Coffee coffee) {
        for (Map.Entry<String, Integer> entry : coffee.getRecipe().entrySet()) {
            ingredients.get(entry.getKey()).updateQuantity(-entry.getValue());
        }
    }

    public synchronized void refillIngredient(String ingredientName, int amount) {
        Ingredient ingredient = ingredients.get(ingredientName.toLowerCase());
        if (ingredient != null) {
            ingredient.updateQuantity(amount);
            System.out.printf("[INFO] Refilled %s by %d units. New quantity: %d%n",
                    ingredientName, amount, ingredient.getQuantity());
        }
    }

    private void checkInventory() {
        for (Ingredient ingredient : ingredients.values()) {
            if (ingredient.isLow()) {
                System.out.printf("[WARNING] Ingredient '%s' is running low (quantity: %d).%n",
                        ingredient.getName(), ingredient.getQuantity());
            }
        }
    }
}

