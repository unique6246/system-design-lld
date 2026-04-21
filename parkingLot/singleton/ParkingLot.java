package singleton;

import model.ParkingFloor;
import model.vehicle.Vehicle;

import java.util.ArrayList;
import java.util.List;

public class ParkingLot {
    private static volatile ParkingLot instance;
    private final List<ParkingFloor> floors;

    private ParkingLot() {
        this.floors = new ArrayList<>();
    }

    /** Double-checked locking for thread-safe singleton. */
    public static ParkingLot getInstance() {
        if (instance == null) {
            synchronized (ParkingLot.class) {
                if (instance == null) {
                    instance = new ParkingLot();
                }
            }
        }
        return instance;
    }

    public void addFloor(ParkingFloor floor) {
        floors.add(floor);
    }

    /**
     * Attempts to park a vehicle on any available floor.
     */
    public synchronized void parkVehicle(Vehicle vehicle) {
        System.out.println("Parking " + vehicle + "...");
        for (ParkingFloor floor : floors) {
            if (floor.parkVehicle(vehicle)) {
                System.out.println("  Successfully parked on Floor " + floor.getFloorNumber());
                return;
            }
        }
        System.out.println("  No available spot for " + vehicle);
    }

    /**
     * Unparks a vehicle from the lot.
     */
    public synchronized void unparkVehicle(Vehicle vehicle) {
        System.out.println("Unparking " + vehicle + "...");
        for (ParkingFloor floor : floors) {
            if (floor.unparkVehicle(vehicle)) {
                System.out.println("  Successfully unparked from Floor " + floor.getFloorNumber());
                return;
            }
        }
        System.out.println("  Vehicle " + vehicle + " not found in lot");
    }

    /** Prints real-time availability across all floors. */
    public synchronized void displayAvailability() {
        System.out.println("=== Parking Lot Availability ===");
        int totalAvailable = 0;
        int totalSpots = 0;
        for (ParkingFloor floor : floors) {
            floor.displayAvailability();
            totalAvailable += floor.getAvailableSpots();
            totalSpots += floor.getTotalSpots();
        }
        System.out.println("Total: " + totalAvailable + "/" + totalSpots + " spots available");
        System.out.println("================================");
    }
}
