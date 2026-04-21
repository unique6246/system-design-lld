package model.vehicle;

import enums.VehicleSize;

public class Car extends Vehicle {
    public Car(String licensePlate) {
        super(licensePlate, VehicleSize.COMPACT);
    }
}
