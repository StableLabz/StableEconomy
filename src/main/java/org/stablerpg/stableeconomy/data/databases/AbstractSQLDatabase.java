package org.stablerpg.stableeconomy.data.databases;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.jetbrains.annotations.NotNull;
import org.stablerpg.stableeconomy.EconomyPlatform;
import org.stablerpg.stableeconomy.data.BalanceEntry;
import org.stablerpg.stableeconomy.data.PlayerAccount;
import org.stablerpg.stableeconomy.data.util.ByteArrayWrapper;
import org.stablerpg.stableeconomy.data.util.DataUtils;
import org.stablerpg.stableeconomy.data.util.DatabaseInfo;

import java.sql.*;
import java.util.HashMap;
import java.util.logging.Level;

public abstract class AbstractSQLDatabase extends Database {

  private HikariDataSource dataSource;

  protected AbstractSQLDatabase(Class<? extends Driver> driver, @NotNull EconomyPlatform platform) {
    super(platform);
    if (DriverManager.drivers().noneMatch(driver::isInstance)) try {
      DriverManager.registerDriver(driver.getDeclaredConstructor().newInstance());
    } catch (Exception exception) {
      throw new RuntimeException("Failed to register SQL driver: " + driver.getName(), exception);
    }
    setup();
  }

  abstract void processConfig(@NotNull HikariConfig hikariConfig, @NotNull DatabaseInfo databaseInfo);

  @Override
  protected void setup() {
    HikariConfig hikariConfig = new HikariConfig();
    hikariConfig.setAutoCommit(false);
    processConfig(hikariConfig, getConfig().getDatabaseInfo());
    dataSource = new HikariDataSource(hikariConfig);

    try (Connection connection = dataSource.getConnection(); Statement statement = connection.createStatement()) {
      statement.execute("CREATE TABLE IF NOT EXISTS player_entries(uniqueId BINARY(16) PRIMARY KEY, username VARCHAR(16));");
      statement.execute("CREATE TABLE IF NOT EXISTS balance_entries(uniqueId BINARY(16), currency VARCHAR(16), balance DOUBLE, FOREIGN KEY(uniqueId) REFERENCES player_entries(uniqueId), UNIQUE(uniqueId, currency));");
      connection.commit();
    } catch (SQLException exception) {
      getConfig().getLogger().log(Level.SEVERE, "Failed to setup SQL database.", exception);
    }
    super.setup();
  }

  @Override
  protected int lookupEntryCount() {
    try (Connection connection = dataSource.getConnection(); Statement statement = connection.createStatement()) {
      ResultSet resultSet = statement.executeQuery("SELECT COUNT(*) FROM player_entries;");
      if (resultSet.next()) return resultSet.getInt(1);
    } catch (SQLException exception) {
      getConfig().getLogger().log(Level.SEVERE, "Failed to lookup entry count from SQL database.", exception);
    }
    return super.lookupEntryCount();
  }

  @Override
  protected void load() {
    try (Connection connection = dataSource.getConnection(); Statement statement = connection.createStatement()) {
      HashMap<ByteArrayWrapper, HashMap<String, BalanceEntry>> balanceEntries = new HashMap<>();

      try (ResultSet rawBalanceEntries = statement.executeQuery("SELECT * FROM balance_entries;")) {
        while (rawBalanceEntries.next()) {
          ByteArrayWrapper rawUniqueId = new ByteArrayWrapper(rawBalanceEntries.getBytes("uniqueId"));

          if (!balanceEntries.containsKey(rawUniqueId)) balanceEntries.put(rawUniqueId, new HashMap<>());

          String currency = rawBalanceEntries.getString("currency");
          double balance = rawBalanceEntries.getDouble("balance");

          balanceEntries.get(rawUniqueId).put(currency, new BalanceEntry(currency, balance));
        }
      }

      try (ResultSet rawPlayerEntries = statement.executeQuery("SELECT * FROM player_entries;")) {
        while (rawPlayerEntries.next()) {
          ByteArrayWrapper rawUniqueId = new ByteArrayWrapper(rawPlayerEntries.getBytes("uniqueId"));

          add(new PlayerAccount(getPlatform(), rawUniqueId.toUUID(), rawPlayerEntries.getString("username"), balanceEntries.getOrDefault(rawUniqueId, new HashMap<>())));
        }
      }

    } catch (SQLException exception) {
      getConfig().getLogger().log(Level.SEVERE, "Failed to load data from SQL database.", exception);
    }
  }

  @Override
  protected void save() {
    try (Connection connection = dataSource.getConnection()) {
      try (PreparedStatement playerStatement = connection.prepareStatement(getSavePlayerStatement()); PreparedStatement balanceStatement = connection.prepareStatement(getSaveBalanceStatement())) {

        for (PlayerAccount playerAccount : entries) {

          try {
            byte[] uniqueId = DataUtils.uuidToBytes(playerAccount.getUniqueId());

            if (playerAccount.isDirty()) {
              playerStatement.setBytes(1, uniqueId);
              playerStatement.setString(2, playerAccount.getUsername());
              playerStatement.addBatch();
            }

            for (BalanceEntry balanceEntry : playerAccount.getBalanceEntries()) {
              if (!balanceEntry.isDirty()) continue;

              balanceStatement.setBytes(1, uniqueId);
              balanceStatement.setString(2, balanceEntry.getCurrency());
              balanceStatement.setDouble(3, balanceEntry.getUnsavedBalance());
              balanceStatement.addBatch();
            }

            playerAccount.markClean();
            playerAccount.getBalanceEntries().forEach(BalanceEntry::markClean);
          } catch (SQLException exception) {
            getConfig().getLogger().log(Level.SEVERE, "Failed to save data for %s (%s)".formatted(playerAccount.getUsername(), playerAccount.getUniqueId()), exception);
          }
        }

        playerStatement.executeBatch();
        balanceStatement.executeBatch();

        connection.commit();
      } catch (SQLException exception) {
        connection.rollback();
        throw exception;
      }
    } catch (SQLException exception) {
      getConfig().getLogger().log(Level.SEVERE, "Failed to save data to SQL database.", exception);
    }
  }

  String getSavePlayerStatement() {
    return "INSERT INTO player_entries(uniqueId, username) VALUES(?, ?) ON DUPLICATE KEY UPDATE username = VALUES(username);";
  }

  String getSaveBalanceStatement() {
    return "INSERT INTO balance_entries(uniqueId, currency, balance) VALUES(?, ?, ?) ON DUPLICATE KEY UPDATE balance = balance_entries.balance + VALUES(balance);";
  }

  @Override
  public void close() {
    super.close();
    dataSource.close();
  }

}
