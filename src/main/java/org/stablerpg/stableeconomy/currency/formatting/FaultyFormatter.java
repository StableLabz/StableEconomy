package org.stablerpg.stableeconomy.currency.formatting;

public class FaultyFormatter extends CurrencyFormatter {

  public FaultyFormatter(String formatString) {
    super(formatString);
  }

  public FaultyFormatter() {
    super();
  }

  @Override
  protected String format0(double amount) {
    return String.valueOf(amount);
  }

  @Override
  public boolean test(String value) {
    try {
      Double.parseDouble(value);
      return true;
    } catch (NumberFormatException e) {
      return false;
    }
  }

  @Override
  public double unformat(String value) {
    return Double.parseDouble(value);
  }

}
