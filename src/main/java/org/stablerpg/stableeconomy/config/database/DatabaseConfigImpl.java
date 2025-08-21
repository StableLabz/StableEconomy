package org.stablerpg.stableeconomy.config.database;

import dev.triumphteam.gui.element.GuiItem;
import dev.triumphteam.gui.paper.Gui;
import dev.triumphteam.gui.paper.builder.item.ItemBuilder;
import dev.triumphteam.nova.MutableState;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Material;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.stablerpg.stableeconomy.AbstractEconomyPlugin;
import org.stablerpg.stableeconomy.config.AbstractConfig;
import org.stablerpg.stableeconomy.data.util.DatabaseInfo;
import org.stablerpg.stableeconomy.gui.config.ConfigInventory;
import org.stablerpg.stableeconomy.gui.config.ConfigItem;

import java.io.IOException;
import java.util.logging.Logger;

@ConfigInventory(
  title = "Database Configuration",
  rows = 3
)
public class DatabaseConfigImpl extends AbstractConfig implements DatabaseConfig {

  @ConfigItem(
    slot = 10,
    displayName = "Database Type"
  )
  private DatabaseInfo.DatabaseType databaseType;
  @ConfigItem(
    slot = 11,
    displayName = "Database Address"
  )
  private String databaseAddress;
  @ConfigItem(
    slot = 12,
    displayName = "Database Port"
  )
  private int databasePort;
  @ConfigItem(
    slot = 13,
    displayName = "Database Name"
  )
  private String databaseName;
  @ConfigItem(
    slot = 14,
    displayName = "Database Username"
  )
  private String databaseUsername;
  @ConfigItem(
    slot = 15,
    displayName = "Database Password"
  )
  private String databasePassword;
  @ConfigItem(
    slot = 16,
    displayName = "Auto-Save Interval (ms)"
  )
  private long autoSaveInterval;

  public DatabaseConfigImpl(@NotNull AbstractEconomyPlugin plugin) {
    super(plugin, "database.yml");
  }

  @Override
  public @NotNull DatabaseInfo getDatabaseInfo() {
    return new DatabaseInfo(databaseType, databaseAddress, databasePort, databaseName, databaseUsername, databasePassword, autoSaveInterval);
  }

  @Override
  public @NotNull Logger getLogger() {
    return getPlugin().getLogger();
  }

  @Override
  public void load() {
    super.load();
    YamlConfiguration config = getConfig();

    databaseType = DatabaseInfo.DatabaseType.fromString(config.getString("database.type", "H2"));
    databaseAddress = config.getString("database.address", "localhost");
    databasePort = config.getInt("database.port", 3306);
    databaseName = config.getString("database.name", "economy");
    databaseUsername = config.getString("database.username", "root");
    databasePassword = config.getString("database.password", "root");

    autoSaveInterval = config.getLong("auto-save-interval", 60000L);
  }

  @Override
  public void open(Player player) {
    MutableState<DatabaseInfo.DatabaseType> databaseType = MutableState.of(this.databaseType);
    MutableState<String> databaseAddress = MutableState.of(this.databaseAddress);
    MutableState<Integer> databasePort = MutableState.of(this.databasePort);
    MutableState<String> databaseName = MutableState.of(this.databaseName);
    MutableState<String> databaseUsername = MutableState.of(this.databaseUsername);
    MutableState<String> databasePassword = MutableState.of(this.databasePassword);

    Gui gui = Gui.of(3)
      .component(component -> {
        component.render(container -> {
          ItemBuilder itemBuilder = switch (databaseType.get()) {
            case H2 -> ItemBuilder.from(Material.COMPASS).name(Component.text("H2", NamedTextColor.GRAY))
              .lore(
                Component.text("default", NamedTextColor.GRAY),
                Component.empty(),
                Component.text("File-based database, suitable for small servers.", NamedTextColor.GRAY),
                Component.empty(),
                Component.text("Click to switch to the next database type.", NamedTextColor.GRAY)
              );
            case SQLITE -> ItemBuilder.from(Material.BUNDLE).name(Component.text("SQLite", NamedTextColor.AQUA))
              .lore(
                Component.text("File-based database, suitable for medium servers.", NamedTextColor.GRAY),
                Component.empty(),
                Component.text("Click to switch to the next database type.", NamedTextColor.GRAY)
              );
            case MYSQL -> ItemBuilder.from(Material.CLOCK).name(Component.text("MySQL", NamedTextColor.GOLD))
              .lore(
                Component.text("Network-based database, suitable for large servers.", NamedTextColor.GRAY),
                Component.empty(),
                Component.text("Click to switch to the next database type.", NamedTextColor.GRAY)
              );
            case MARIADB -> ItemBuilder.from(Material.CHEST).name(Component.text("MariaDB", NamedTextColor.BLUE))
              .lore(
                Component.text("Network-based database, suitable for large servers.", NamedTextColor.GRAY),
                Component.empty(),
                Component.text("Click to switch to the next database type.", NamedTextColor.GRAY)
              );
            case POSTGRESQL ->
              ItemBuilder.from(Material.PAPER).name(Component.text("PostgreSQL", NamedTextColor.DARK_AQUA))
                .lore(
                  Component.text("Network-based database, suitable for large servers.", NamedTextColor.GRAY),
                  Component.empty(),
                  Component.text("Click to switch to the next database type.", NamedTextColor.GRAY)
                );
            case MONGODB ->
              ItemBuilder.from(Material.BIG_DRIPLEAF).name(Component.text("MongoDB", NamedTextColor.GREEN))
                .lore(
                  Component.text("NoSQL database, suitable for large servers.", NamedTextColor.GRAY),
                  Component.empty(),
                  Component.text("Click to switch to the next database type.", NamedTextColor.GRAY)
                );
          };
          GuiItem<Player, ItemStack> guiItem = itemBuilder.asGuiItem((clicker, context) -> databaseType.set(databaseType.get().next()));
          container.setItem(10, guiItem);
          guiItem = ItemBuilder.from(Material.TARGET)
            .name(Component.text("Database Address", NamedTextColor.GRAY))
            .lore(
              Component.text("Current: " + databaseAddress.get(), NamedTextColor.GRAY),
              Component.empty(),
              Component.text("Click to edit the database address.", NamedTextColor.GRAY)
            )
            .asGuiItem((clicker, context) -> {
            });
          container.setItem(11, guiItem);
        });
      })
      .onClose(() -> {
        getConfig().set("database.type", databaseType.get().name());
        getConfig().set("database.address", databaseAddress.get());
        getConfig().set("database.port", databasePort.get());
        getConfig().set("database.name", databaseName.get());
        getConfig().set("database.username", databaseUsername.get());
        getConfig().set("database.password", databasePassword.get());
        try {
          getConfig().save(getFile());
        } catch (IOException e) {
          player.sendRichMessage("<red>Failed to save database configuration: %s".formatted(e.getMessage()));
          getLogger().severe("Failed to save database configuration: " + e.getMessage());
        }
      })
      .build();
    gui.open(player);
  }

}
