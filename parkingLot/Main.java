
import model.ParkingFloor;
import model.vehicle.Car;
import model.vehicle.Motorcycle;
import model.vehicle.Truck;
import singleton.ParkingLot;

public class Main {

    public static void main(String[] args) throws InterruptedException {

        // --- Setup ---
        ParkingLot lot = ParkingLot.getInstance();

        // Floor 1: 2 motorcycle, 3 compact, 1 large spot
        lot.addFloor(new ParkingFloor(1, 2, 3, 1));
        // Floor 2: 1 motorcycle, 2 compact, 2 large spots
        lot.addFloor(new ParkingFloor(2, 1, 2, 2));

        System.out.println("\n--- Initial Availability ---");
        lot.displayAvailability();

        // --- Single-threaded parking ---
        Car car1       = new Car("CAR-001");
        Car car2       = new Car("CAR-002");
        Motorcycle m1  = new Motorcycle("MOTO-001");
        Motorcycle m2  = new Motorcycle("MOTO-002");
        Truck t1       = new Truck("TRUCK-001");
        Truck t2       = new Truck("TRUCK-002");

        System.out.println("\n--- Parking Vehicles ---");
        lot.parkVehicle(car1);
        lot.parkVehicle(car2);
        lot.parkVehicle(m1);
        lot.parkVehicle(m2);
        lot.parkVehicle(t1);
        lot.parkVehicle(t2);

        System.out.println("\n--- Availability After Parking ---");
        lot.displayAvailability();

        // --- Unparking ---
        System.out.println("\n--- Unparking ---");
        lot.unparkVehicle(car1);
        lot.unparkVehicle(t1);

        System.out.println("\n--- Availability After Unparking ---");
        lot.displayAvailability();

        // --- Concurrent access simulation ---
        System.out.println("\n--- Concurrent Access Simulation ---");
        Car car3 = new Car("CAR-003");
        Car car4 = new Car("CAR-004");

        Thread t3 = new Thread(() -> lot.parkVehicle(car3), "Entry-Gate-1");
        Thread t4 = new Thread(() -> lot.parkVehicle(car4), "Entry-Gate-2");
        Thread t5 = new Thread(() -> lot.unparkVehicle(m1),  "Exit-Gate-1");

        t3.start();
        t4.start();
        t5.start();

        t3.join();
        t4.join();
        t5.join();

        System.out.println("\n--- Final Availability ---");
        lot.displayAvailability();
    }
}
