package org.stablerpg.stableeconomy.service;

import lombok.Getter;
import org.bukkit.NamespacedKey;
import org.jetbrains.annotations.NotNull;
import org.stablerpg.stableeconomy.EconomyPlatform;

@Getter
public abstract class AbstractDatabaseService implements DatabaseService {

  private final EconomyPlatform platform;
  private final NamespacedKey key;

  public AbstractDatabaseService(@NotNull EconomyPlatform platform) {
    this.platform = platform;
    this.key = new NamespacedKey("stableeconomy", "database");
  }

  protected abstract void load();

  protected abstract void save();

}
