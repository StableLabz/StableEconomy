package org.stablerpg.stableeconomy.currency.formatting;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;
import java.util.Optional;

@AllArgsConstructor
@Getter
public enum Formatters {

  STABLE(new StableFormatter()),
  COMMA(new CommaFormatter()),
  SUFFIX(new SuffixFormatter()),
  FAULTY(new FaultyFormatter());

  private final CurrencyFormatter formatter;

  public String format(double amount) {
    return formatter.format(amount);
  }

  public static Formatters fromString(String name) {
    return switch (name.toUpperCase()) {
      case "STABLE" -> STABLE;
      case "COMMA" -> COMMA;
      case "SUFFIX" -> SUFFIX;
      default -> FAULTY;
    };
  }

  public static Optional<Double> unformat(String value) {
    return Arrays.stream(Formatters.values())
      .map(Formatters::getFormatter)
      .filter(formatter -> formatter.test(value))
      .map(formatter -> formatter.unformat(value))
      .findFirst();
  }

}
