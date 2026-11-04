package com.disruptioncompanion.domain.model;

import java.time.*;

public record FareConditions(Boolean changeable, Boolean refundable, String fareFamily) {}
