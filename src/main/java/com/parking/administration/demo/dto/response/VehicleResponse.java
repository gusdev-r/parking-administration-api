package com.parking.administration.demo.dto.response;

import java.time.LocalDateTime;

public record VehicleResponse(String brand, String model, String color, String licensePlateNumber,
                               Long id, LocalDateTime createdAt) {
}
