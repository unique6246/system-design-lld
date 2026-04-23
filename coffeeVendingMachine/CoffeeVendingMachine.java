

import model.Payment;
import singleton.CoffeeMachine;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class CoffeeVendingMachine {

    public static void main(String[] args) throws InterruptedException {
        CoffeeMachine machine = CoffeeMachine.getInstance();

        // Display the menu
        machine.displayMenu();

        // Simulate concurrent user requests
        ExecutorService executor = Executors.newFixedThreadPool(5);

        executor.submit(() -> machine.selectCoffee("espresso",   new Payment(3.00)));
        executor.submit(() -> machine.selectCoffee("cappuccino", new Payment(3.50)));
        executor.submit(() -> machine.selectCoffee("latte",      new Payment(5.00)));
        executor.submit(() -> machine.selectCoffee("espresso",   new Payment(2.00))); // insufficient payment
        executor.submit(() -> machine.selectCoffee("mocha",      new Payment(4.00))); // not in menu

        executor.shutdown();
        executor.awaitTermination(10, TimeUnit.SECONDS);

        System.out.println("\n--- Refilling milk ---");
        machine.refillIngredient("milk", 10);
    }
}

