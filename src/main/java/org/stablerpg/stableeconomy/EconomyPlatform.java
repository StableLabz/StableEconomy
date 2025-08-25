package org.stablerpg.stableeconomy;

import dev.jorel.commandapi.CommandTree;
import dev.jorel.commandapi.arguments.LiteralArgument;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.stablerpg.stableeconomy.api.EconomyAPI;
import org.stablerpg.stableeconomy.api.PriceProvider;
import org.stablerpg.stableeconomy.config.currency.CurrencyConfig;
import org.stablerpg.stableeconomy.config.currency.CurrencyHolder;
import org.stablerpg.stableeconomy.config.database.DatabaseConfig;
import org.stablerpg.stableeconomy.config.database.DatabaseConfigImpl;
import org.stablerpg.stableeconomy.config.prices.PriceConfig;
import org.stablerpg.stableeconomy.config.prices.PriceConfigImpl;
import org.stablerpg.stableeconomy.config.shop.ShopConfig;
import org.stablerpg.stableeconomy.config.shop.ShopConfigImpl;
import org.stablerpg.stableeconomy.currency.Currency;
import org.stablerpg.stableeconomy.data.PlayerAccount;
import org.stablerpg.stableeconomy.data.databases.Database;
import org.stablerpg.stableeconomy.hooks.PlaceholderAPIHook;
import org.stablerpg.stableeconomy.hooks.VaultHook;
import org.stablerpg.stableeconomy.shop.ShopManager;

import java.io.Closeable;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.logging.Logger;

public class EconomyPlatform implements EconomyAPI, Closeable {

  @Getter
  private final AbstractEconomyPlugin plugin;

  @Getter
  private final DatabaseConfig config;
  @Getter
  private final CurrencyHolder currencyHolder;
  @Getter
  private final PriceConfig priceConfig;
  @Getter
  private final ShopConfig shopConfig;

  private Database database;

  private VaultHook vaultHook;
  private PlaceholderAPIHook placeholderAPIHook;

  private final CommandTree command;

  public EconomyPlatform(AbstractEconomyPlugin plugin, DatabaseConfig config, CurrencyHolder currencyHolder, PriceConfig priceConfig, ShopConfig shopConfig, CommandTree command) {
    this.plugin = plugin;
    this.config = config;
    this.currencyHolder = currencyHolder;
    this.priceConfig = priceConfig;
    this.shopConfig = shopConfig;
    this.command = command;
  }

  public EconomyPlatform(AbstractEconomyPlugin plugin) {
    this.plugin = plugin;
    this.config = new DatabaseConfigImpl(plugin);
    this.currencyHolder = new CurrencyConfig(this);
    this.priceConfig = new PriceConfigImpl(plugin);
    this.shopConfig = new ShopConfigImpl(this);
    this.command = new CommandTree(plugin.getName().toLowerCase())
      .then(LiteralArgument.of("reload")
        .executes((sender, args) -> {
          close();
          init();
        })
      )
      .then(LiteralArgument.of("editConfig")
        .then(LiteralArgument.of("database")
          .executesPlayer(((player, args) -> {
            config.open(player);
          }))
        )
      );
  }

  public void init() {
    config.load();
    currencyHolder.load();
    priceConfig.load();
    shopConfig.load();

    database = Database.of(this);

    loadHooks();
    Bukkit.getGlobalRegionScheduler().runDelayed(plugin, task -> loadHooks(), 20L);
  }

  private void loadHooks() {
    if (Bukkit.getPluginManager().isPluginEnabled("Vault")) vaultHook = new VaultHook(this);
    if (Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")) placeholderAPIHook = new PlaceholderAPIHook(this);
  }

  @Override
  public void close() {
    shopConfig.close();
    priceConfig.close();
    if (placeholderAPIHook != null) {
      placeholderAPIHook.close();
      placeholderAPIHook = null;
    }
    if (vaultHook != null) {
      vaultHook.close();
      vaultHook = null;
    }
    currencyHolder.close();
    database.close();
    database = null;
  }

  public Logger getLogger() {
    return plugin.getLogger();
  }

  @Override
  public PlayerAccount getAccount(UUID uniqueId) {
    return database.getAccount(uniqueId).join();
  }

  @Override
  public PlayerAccount getAccount(String username) {
    return database.getAccount(username).join();
  }

  @Override
  public double getBalance(UUID uniqueId, String currency) {
    return database.query(uniqueId, account -> account.getBalance(currency)).join();
  }

  @Override
  public double getBalance(String username, String currency) {
    return database.query(username, account -> account.getBalance(currency)).join();
  }

  @Override
  public void setBalance(UUID uniqueId, double amount, String currency) {
    database.update(uniqueId, account -> account.setBalance(currency, amount));
  }

  @Override
  public void setBalance(String username, double amount, String currency) {
    database.update(username, account -> account.setBalance(currency, amount));
  }

  @Override
  public void addBalance(UUID uniqueId, double amount, String currency) {
    database.update(uniqueId, account -> account.addBalance(currency, amount));
  }

  @Override
  public void addBalance(String username, double amount, String currency) {
    database.update(username, account -> account.addBalance(currency, amount));
  }

  @Override
  public void subtractBalance(UUID uniqueId, double amount, String currency) {
    database.update(uniqueId, account -> account.subtractBalance(currency, amount));
  }

  @Override
  public void subtractBalance(String username, double amount, String currency) {
    database.update(username, account -> account.subtractBalance(currency, amount));
  }

  @Override
  public void resetBalance(UUID uniqueId, String currency) {
    database.update(uniqueId, account -> account.resetBalance(currency));
  }

  @Override
  public void resetBalance(String username, String currency) {
    database.update(username, account -> account.resetBalance(currency));
  }

  @Override
  public List<PlayerAccount> getLeaderboard(String currency) {
    return database.sortedByBalance(currency);
  }

  @Override
  public Optional<Currency> getCurrency(String currency) {
    return currencyHolder.getCurrency(currency);
  }

  @Override
  public PriceProvider getPriceProvider() {
    return priceConfig.getPriceProvider();
  }

  @Override
  public ShopManager getShopManager() {
    return shopConfig.getShopManager();
  }

}
