package org.stablerpg.stableeconomy.currency.formatting;

public enum Formatters {

  COOL, COMMA, SUFFIX, FAULTY;

  public static Formatters fromString(String name) {
    return switch (name.toUpperCase()) {
      case "COOL" -> COOL;
      case "COMMA" -> COMMA;
      case "SUFFIX" -> SUFFIX;
      default -> FAULTY;
    };
  }

}
