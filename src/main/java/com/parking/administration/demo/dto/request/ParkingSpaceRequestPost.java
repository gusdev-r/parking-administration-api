package com.parking.administration.demo.dto.request;

public record ParkingSpaceRequestPost(String condominiumBlock, String condominiumApartment, String vehicleBrand, String vehicleModel,
                                      String vehicleColor , String responsibleVehicleName, String vehicleSpaceNumber,
                                      String vehicleLicensePlateNumber) {
}
