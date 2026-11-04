package com.disruptioncompanion.application.port.out;

public class SourceUnavailableException extends RuntimeException {
  public SourceUnavailableException() {
    super("Required source unavailable");
  }
}
