package org.stablerpg.stableeconomy.config;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.io.Closeable;
import java.util.logging.Logger;

public interface BasicConfig extends Closeable {

  void load();

  void close();

  @NotNull Logger getLogger();

  void open(Player player);

}
