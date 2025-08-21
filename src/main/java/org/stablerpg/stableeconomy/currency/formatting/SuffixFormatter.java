package org.stablerpg.stableeconomy.currency.formatting;

import java.text.DecimalFormat;
import java.util.List;
import java.util.regex.Pattern;

public class SuffixFormatter extends CurrencyFormatter {

  private static final DecimalFormat DECIMAL_FORMAT = new DecimalFormat("#.##");
  private static final List<String> SUFFIXES = List.of("", "k", "M", "B", "T", "Q", "Qi", "Sx", "Sp", "O", "N", "D");

  private static final Pattern SUFFIX_PATTERN = Pattern.compile("^(\\d+(\\.\\d+)?)([kMBTQOND]?|Qi|Sx|Sp)?$");

  public SuffixFormatter(String formatString) {
    super(formatString);
  }

  public SuffixFormatter() {
    super();
  }

  @Override
  protected String format0(double amount) {
    int index;
    for (index = 0; index < SUFFIXES.size() - 1 && amount >= 1000; index++)
      amount /= 1000;
    return DECIMAL_FORMAT.format(amount) + SUFFIXES.get(index);
  }

  @Override
  public boolean test(String value) {
    return SUFFIX_PATTERN.matcher(value).matches();
  }

  @Override
  public double unformat(String value) {
    String number = value.replaceAll("[^\\d.]", "");
    String suffix = value.replaceAll("[\\d.]", "").toUpperCase();
    int index = SUFFIXES.indexOf(suffix);
    if (index == -1)
      throw new NumberFormatException("Invalid suffix");
    double multiplier = Math.pow(1000, index);
    return Double.parseDouble(number) * multiplier;
  }

}
