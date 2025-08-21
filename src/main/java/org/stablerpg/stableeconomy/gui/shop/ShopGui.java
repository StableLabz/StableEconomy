package org.stablerpg.stableeconomy.gui.shop;

import dev.triumphteam.gui.paper.Gui;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.stablerpg.stableeconomy.gui.AbstractGui;
import org.stablerpg.stableeconomy.gui.AbstractLinkedGui;
import org.stablerpg.stableeconomy.shop.backend.ShopCategory;

public class ShopGui extends AbstractLinkedGui<ShopCategory> {

  public static void open(@NotNull ShopCategory category, @NotNull Player player) {
    new ShopGui(category).open(player);
  }

  protected ShopGui(@Nullable AbstractGui<ShopCategory> parent, @NotNull ShopCategory context) {
    super(parent, context);
  }

  protected ShopGui(ShopCategory context) {
    super(context);
  }

  @Override
  protected Gui generate(@NotNull Player player, @Nullable AbstractGui<ShopCategory> parent, @NotNull ShopCategory category) {
    return Gui.of(category.getRows())
      .title(category.getTitle())
      .statelessComponent(container -> category.drawGui(container, player)).build();
  }

}
