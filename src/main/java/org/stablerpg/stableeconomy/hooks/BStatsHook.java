package org.stablerpg.stableeconomy.hooks;

import org.bstats.bukkit.Metrics;
import org.bstats.charts.SimplePie;
import org.stablerpg.stableeconomy.AbstractEconomyPlugin;

public class BStatsHook extends AbstractHook {

  private Metrics metrics;

  public BStatsHook(AbstractEconomyPlugin plugin) {
    super(plugin);
  }

  @Override
  public boolean canLoad() {
    return true;
  }

  @Override
  public void load() {
    metrics = new Metrics(getPlugin(), -1);
    metrics.addCustomChart(new SimplePie("database", () -> getPlugin().getEconomyPlatform().getConfig().getDatabaseInfo().getDatabaseType().name()));
  }

  @Override
  public void close() {
    metrics.shutdown();
    metrics = null;
  }

}
