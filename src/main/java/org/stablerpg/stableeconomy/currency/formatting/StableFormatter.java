package org.stablerpg.stableeconomy.currency.formatting;

public class StableFormatter extends CurrencyFormatter {

  private final CommaFormatter comma;
  private final SuffixFormatter suffix;

  public StableFormatter(String formatString) {
    super(formatString);
    comma = new CommaFormatter(formatString);
    suffix = new SuffixFormatter(formatString);
  }

  public StableFormatter() {
    super();
    comma = new CommaFormatter();
    suffix = new SuffixFormatter();
  }

  @Override
  protected String format0(double amount) {
    return amount < 1_000_000.0 ? comma.format0(amount) : suffix.format0(amount);
  }

  @Override
  public boolean test(String value) {
    try {
      return comma.test(value) || suffix.test(value);
    } catch (NumberFormatException e) {
      return false;
    }
  }

  @Override
  public double unformat(String value) {
    try {
      return comma.unformat(value);
    } catch (NumberFormatException e) {
      return suffix.unformat(value);
    }
  }

}
