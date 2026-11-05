package com.disruptioncompanion.domain.model;

import com.disruptioncompanion.domain.enums.PassengerType;

public record Passenger(String passengerId, PassengerType type, Integer age) {
}
