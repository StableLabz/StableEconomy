package org.stablerpg.stableeconomy.gui;

import dev.triumphteam.gui.paper.Gui;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class AbstractLinkedGui<T> {

  private final @Nullable AbstractGui<T> parent;
  private final @NotNull T context;

  protected AbstractLinkedGui(@Nullable AbstractGui<T> parent, @NotNull T context) {
    this.parent = parent;
    this.context = context;
  }

  protected AbstractLinkedGui(@NotNull T context) {
    this(null, context);
  }

  private Gui generate(Player player) {
    return generate(player, parent, context);
  }

  protected abstract Gui generate(@NotNull Player player, @Nullable AbstractGui<T> parent, @NotNull T context);

  public final void open(Player player) {
    generate(player).open(player);
  }
}
