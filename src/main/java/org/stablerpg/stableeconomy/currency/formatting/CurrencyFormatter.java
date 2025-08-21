package org.stablerpg.stableeconomy.currency.formatting;

import java.util.function.Predicate;

public abstract class CurrencyFormatter implements Predicate<String> {

  protected final String formatString;

  public CurrencyFormatter(String formatString) {
    this.formatString = formatString.replaceAll("<amount>", "%s");
  }

  public CurrencyFormatter() {
    this("%s");
  }

  public static CurrencyFormatter of(Formatters formatter, String formatString) {
    return switch (formatter) {
      case STABLE -> new StableFormatter(formatString);
      case COMMA -> new CommaFormatter(formatString);
      case SUFFIX -> new SuffixFormatter(formatString);
      case FAULTY -> new FaultyFormatter(formatString);
    };
  }

  public String format(double amount) {
    return formatString.formatted(format0(amount));
  }

  protected abstract String format0(double amount);

  public abstract boolean test(String value);

  public abstract double unformat(String value);

}
