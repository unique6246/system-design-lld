package model;

import enums.VehicleSize;
import model.vehicle.Vehicle;

public class ParkingSpot {
    private final int spotId;
    private final VehicleSize spotSize;
    private Vehicle parkedVehicle;

    public ParkingSpot(int spotId, VehicleSize spotSize) {
        this.spotId = spotId;
        this.spotSize = spotSize;
        this.parkedVehicle = null;
    }

    public synchronized boolean isAvailable() {
        return parkedVehicle == null;
    }

    public synchronized boolean canFitVehicle(Vehicle vehicle) {
        return isAvailable() && spotSize == vehicle.getSize();
    }

    public synchronized void assignVehicle(Vehicle vehicle) {
        if (!canFitVehicle(vehicle)) {
            return;
        }
        this.parkedVehicle = vehicle;
        System.out.println("  Assigned " + vehicle + " to Spot #" + spotId + " [" + spotSize + "]");
    }

    public synchronized void removeVehicle(Vehicle vehicle) {
        if (parkedVehicle == null || !parkedVehicle.getLicensePlate().equals(vehicle.getLicensePlate())) {
            return;
        }
        System.out.println("  Removed " + vehicle + " from Spot #" + spotId + " [" + spotSize + "]");
        this.parkedVehicle = null;
    }

    public int getSpotId() {
        return spotId;
    }

    public VehicleSize getSpotSize() {
        return spotSize;
    }

    public Vehicle getParkedVehicle() {
        return parkedVehicle;
    }
}
