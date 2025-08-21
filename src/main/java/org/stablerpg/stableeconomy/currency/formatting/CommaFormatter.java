package org.stablerpg.stableeconomy.currency.formatting;

import java.text.DecimalFormat;
import java.util.regex.Pattern;

public class CommaFormatter extends CurrencyFormatter {

  private static final DecimalFormat COMMA_FORMAT = new DecimalFormat("#,###.##");
  private static final Pattern COMMA_PATTERN = Pattern.compile("^(\\d{1,3}(,\\d{3})*|\\d+)(\\.\\d+)?$");

  public CommaFormatter(String formatString) {
    super(formatString);
  }

  public CommaFormatter() {
    super();
  }

  @Override
  protected String format0(double amount) {
    return COMMA_FORMAT.format(amount);
  }

  @Override
  public boolean test(String value) {
    return COMMA_PATTERN.matcher(value).matches();
  }

  @Override
  public double unformat(String value) {
    String sanitized = value.replace(",", "");
    return Double.parseDouble(sanitized);
  }

}
