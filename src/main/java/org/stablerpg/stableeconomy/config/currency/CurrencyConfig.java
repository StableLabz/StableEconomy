package org.stablerpg.stableeconomy.config.currency;

import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.stablerpg.stableeconomy.EconomyPlatform;
import org.stablerpg.stableeconomy.config.exceptions.DeserializationException;
import org.stablerpg.stableeconomy.currency.Currency;

import java.io.File;
import java.util.*;
import java.util.logging.Logger;

public final class CurrencyConfig implements CurrencyHolder {

  private final @NotNull EconomyPlatform platform;

  private final @NotNull File currencyDir;
  private final Map<String, Currency> currencies = new HashMap<>();
  private Currency defaultCurrency;

  public CurrencyConfig(@NotNull EconomyPlatform platform) {
    this.platform = platform;
    this.currencyDir = new File(platform.getPlugin().getDataFolder(), "currencies");
  }

  @Override
  public void load() {
    defaultCurrency = null;
    currencies.clear();

    if (!currencyDir.exists() || !currencyDir.isDirectory()) {
      platform.getPlugin().saveResource("currencies/default/currency.yml", false);
      platform.getPlugin().saveResource("currencies/default/locale.yml", false);
    }

    File[] currencyDirs = currencyDir.listFiles(File::isDirectory);

    if (currencyDirs == null) {
      platform.getLogger().warning("No currency directories found in " + currencyDir.getAbsolutePath() + ". Please ensure the directory exists and contains currency files.");
      return;
    }

    for (File currencyDir : currencyDirs) {
      File currencyFile = new File(currencyDir, "currency.yml");
      File localeFile = new File(currencyDir, "locale.yml");

      YamlConfiguration currencyConfig = YamlConfiguration.loadConfiguration(currencyFile);
      YamlConfiguration localeConfig = YamlConfiguration.loadConfiguration(localeFile);

      Currency currency;
      try {
        currency = Currency.deserialize(platform, currencyConfig, localeConfig);
      } catch (DeserializationException e) {
        getLogger().warning("Failed to deserialize currency " + currencyFile.getName() + ": " + e.getMessage());
        continue;
      }

      if (currency.isDefaultCurrency())
        defaultCurrency = currency;
      currencies.put(currency.getId(), currency);
    }

    for (Currency currency : currencies.values())
      currency.register();
  }

  @Override
  public void close() {
    for (Currency currency : currencies.values())
      currency.unregister();
    currencies.clear();
  }

  @Override
  public @NotNull Logger getLogger() {
    return platform.getLogger();
  }

  @Override
  public void open(Player player) {

  }

  @Override
  public Currency getDefaultCurrency() {
    return defaultCurrency;
  }

  @Override
  public Collection<Currency> getCurrencies() {
    return Collections.unmodifiableCollection(currencies.values());
  }

  @Override
  public Optional<Currency> getCurrency(@NotNull String id) {
    return Optional.ofNullable(currencies.get(id));
  }

}
