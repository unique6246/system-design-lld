package model;

import enums.VehicleSize;
import model.vehicle.Vehicle;

import java.util.ArrayList;
import java.util.List;

public class ParkingFloor {
    private final int floorNumber;
    private final List<ParkingSpot> spots;

    public ParkingFloor(int floorNumber, int motorcycleSpots, int compactSpots, int largeSpots) {
        this.floorNumber = floorNumber;
        this.spots = new ArrayList<>();
        int id = 1;
        for (int i = 0; i < motorcycleSpots; i++) spots.add(new ParkingSpot(id++, VehicleSize.MOTORCYCLE));
        for (int i = 0; i < compactSpots; i++)    spots.add(new ParkingSpot(id++, VehicleSize.COMPACT));
        for (int i = 0; i < largeSpots; i++)       spots.add(new ParkingSpot(id++, VehicleSize.LARGE));
    }

    public synchronized boolean parkVehicle(Vehicle vehicle) {
        for (ParkingSpot spot : spots) {
            if (spot.canFitVehicle(vehicle)) {
                spot.assignVehicle(vehicle);
                return true;
            }
        }
        return false;
    }

    public synchronized boolean unparkVehicle(Vehicle vehicle) {
        for (ParkingSpot spot : spots) {
            if (!spot.isAvailable() && spot.getParkedVehicle().getLicensePlate().equals(vehicle.getLicensePlate())) {
                spot.removeVehicle(vehicle);
                return true;
            }
        }
        return false;
    }

    public synchronized int getAvailableSpots() {
        int count = 0;
        for (ParkingSpot spot : spots) {
            if (spot.isAvailable()) count++;
        }
        return count;
    }

    public synchronized int getTotalSpots() {
        return spots.size();
    }

    public int getFloorNumber() {
        return floorNumber;
    }

    public synchronized void displayAvailability() {
        System.out.println("  Floor " + floorNumber + ": " + getAvailableSpots() + "/" + getTotalSpots() + " spots available");
        for (ParkingSpot spot : spots) {
            System.out.println("    Spot #" + spot.getSpotId() + " [" + spot.getSpotSize() + "]: "
                    + (spot.isAvailable() ? "AVAILABLE" : "OCCUPIED by " + spot.getParkedVehicle()));
        }
    }
}
