package com.disruptioncompanion.domain.model;

import java.util.List;

public record OperationalEvents(List<OperationalEvent> events, Metadata _meta) {}
