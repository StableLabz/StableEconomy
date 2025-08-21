package org.stablerpg.stableeconomy.data.util;

import lombok.Getter;
import org.stablerpg.stableeconomy.StableEconomy;

@Getter
public final class DatabaseInfo {

  private final DatabaseType databaseType;
  private final String address;
  private final int port;
  private final String path = StableEconomy.class.getSimpleName();
  private final String name;
  private final String username;
  private final String password;
  private final long autoSaveInterval;

  public DatabaseInfo(DatabaseType databaseType, String databaseAddress, int databasePort, String name, String username, String password, long autoSaveInterval) {
    this.databaseType = databaseType;
    this.address = databaseAddress;
    this.port = databasePort;
    this.name = name;
    this.username = username;
    this.password = password;
    this.autoSaveInterval = autoSaveInterval;
  }

  public String getUrl() {
    return "%s:%d".formatted(address, port);
  }

  public String getFullPath() {
    return "./plugins/%s/%s".formatted(path, name);
  }

  public enum DatabaseType {
    SQLITE, H2, MYSQL, MARIADB, POSTGRESQL, MONGODB;

    public static DatabaseType fromString(String type) {
      return switch (type.toUpperCase()) {
        case "SQLITE" -> SQLITE;
        case "MYSQL" -> MYSQL;
        case "MARIADB" -> MARIADB;
        case "POSTGRESQL" -> POSTGRESQL;
        case "MONGODB" -> MONGODB;
        default -> H2;
      };
    }

    public DatabaseType next() {
      return switch (this) {
        case H2 -> SQLITE;
        case SQLITE -> MYSQL;
        case MYSQL -> MARIADB;
        case MARIADB -> POSTGRESQL;
        case POSTGRESQL -> MONGODB;
        case MONGODB -> H2;
      };

    }

  }

}
