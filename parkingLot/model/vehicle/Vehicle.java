package model.vehicle;

import enums.VehicleSize;

public abstract class Vehicle {
    protected String licensePlate;
    protected VehicleSize size;

    public Vehicle(String licensePlate, VehicleSize size) {
        this.licensePlate = licensePlate;
        this.size = size;
    }

    public String getLicensePlate() {
        return this.licensePlate;
    }

    public VehicleSize getSize() {
        return this.size;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "[" + licensePlate + "]";
    }
}
