package org.stablerpg.stableeconomy.gui;

import dev.triumphteam.gui.click.ClickContext;
import dev.triumphteam.gui.element.GuiItem;
import dev.triumphteam.gui.paper.builder.item.ItemBuilder;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.stablerpg.stableeconomy.EconomyPlatform;
import org.stablerpg.stableeconomy.config.exceptions.DeserializationException;
import org.stablerpg.stableeconomy.config.shop.ShopLocale;
import org.stablerpg.stableeconomy.currency.Currency;
import org.stablerpg.stableeconomy.shop.ItemFormatter;
import org.stablerpg.stableeconomy.shop.backend.ShopItem;
import org.stablerpg.stableeconomy.shop.backend.TransactableItem;

public interface AbstractGuiItem {

  static AbstractGuiItem deserialize(EconomyPlatform platform, Currency currency, int slot, ConfigurationSection section, ItemFormatter itemFormatter, ShopLocale locale) throws DeserializationException {
    if (section.isConfigurationSection("item"))
      return TransactableItem.deserialize(platform, currency, slot, section, itemFormatter, locale);
    return ShopItem.deserialize(platform, slot, section, itemFormatter);
  }

  int slot();

  ItemStack build(Player player);

  void execute(Player player, ClickContext context);

  default GuiItem<Player, ItemStack> asGuiItem(Player player) {
    return ItemBuilder.from(build(player)).asGuiItem(this::execute);
  }

}
