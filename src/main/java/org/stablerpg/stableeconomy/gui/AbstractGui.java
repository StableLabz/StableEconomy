package org.stablerpg.stableeconomy.gui;

import dev.triumphteam.gui.paper.Gui;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public abstract class AbstractGui<T> {

  private final @NotNull T context;

  protected AbstractGui(@NotNull T context) {
    this.context = context;
  }

  private Gui generate(Player player) {
    return generate(player, context);
  }

  protected abstract Gui generate(@NotNull Player player, @NotNull T context);

  public final void open(Player player) {
    generate(player).open(player);
  }

}
