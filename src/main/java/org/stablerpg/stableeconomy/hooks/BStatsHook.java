package org.stablerpg.stableeconomy.hooks;

import org.bstats.bukkit.Metrics;
import org.bstats.charts.SimplePie;
import org.stablerpg.stableeconomy.AbstractEconomyPlugin;

public class BStatsHook {

  public static void load(AbstractEconomyPlugin plugin) {
    new Metrics(plugin, -1)
      .addCustomChart(new SimplePie("database", () -> plugin.getEconomyPlatform().getConfig().getDatabaseInfo().getDatabaseType().name()));
  }

}
