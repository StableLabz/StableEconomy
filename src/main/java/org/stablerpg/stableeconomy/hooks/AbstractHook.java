package org.stablerpg.stableeconomy.hooks;

import org.stablerpg.stableeconomy.AbstractEconomyPlugin;

import java.util.logging.Logger;

public abstract class AbstractHook implements Hook {

  private final AbstractEconomyPlugin plugin;

  protected AbstractHook(AbstractEconomyPlugin plugin) {
    this.plugin = plugin;
  }

  protected AbstractEconomyPlugin getPlugin() {
    return plugin;
  }

  protected Logger getLogger() {
    return plugin.getLogger();
  }

  @Override
  public abstract boolean canLoad();

  @Override
  public abstract void load();

  @Override
  public abstract void close();

}
