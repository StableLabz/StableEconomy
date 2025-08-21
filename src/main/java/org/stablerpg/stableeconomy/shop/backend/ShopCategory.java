package org.stablerpg.stableeconomy.shop.backend;

import dev.triumphteam.gui.container.GuiContainer;
import dev.triumphteam.gui.element.GuiItem;
import lombok.Getter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.stablerpg.stableeconomy.EconomyPlatform;
import org.stablerpg.stableeconomy.config.exceptions.DeserializationException;
import org.stablerpg.stableeconomy.config.shop.ShopLocale;
import org.stablerpg.stableeconomy.currency.Currency;
import org.stablerpg.stableeconomy.gui.AbstractGuiItem;
import org.stablerpg.stableeconomy.shop.ItemFormatter;
import org.stablerpg.stableeconomy.shop.ShopManager;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

@Getter
public class ShopCategory {

  public static ShopCategory deserialize(ShopManager manager, ConfigurationSection section, ItemFormatter itemFormatter) throws DeserializationException {
    EconomyPlatform platform = manager.getPlatform();

    String rawCurrency = section.getString("currency", "default");
    Optional<Currency> optionalCurrency = platform.getCurrency(rawCurrency);
    if (optionalCurrency.isEmpty())
      throw new DeserializationException("Failed to locate currency \"%s\"".formatted(rawCurrency));
    Currency currency = optionalCurrency.get();

    String rawTitle = section.getString("title");
    if (rawTitle == null)
      throw new DeserializationException("Failed to locate title");
    Component title = MiniMessage.miniMessage().deserialize(rawTitle);

    int rows = section.getInt("rows", 3);

    ConfigurationSection backgroundItemSection = section.getConfigurationSection("background-item");
    if (backgroundItemSection == null)
      throw new DeserializationException("Failed to locate background item section");
    ItemBuilder backgroundItem = ItemBuilder.deserialize(backgroundItemSection);

    int[] backgroundSlots = section.getIntegerList("background-slots").stream().mapToInt(Integer::intValue).toArray();

    itemFormatter = ItemFormatter.deserialize(section, itemFormatter);

    String localeId = section.getString("locale");
    if (localeId == null)
      throw new DeserializationException("Failed to locate locale id");
    ShopLocale locale = manager.getLocale(localeId);
    if (locale == null)
      throw new DeserializationException("Failed to locate locale \"%s\"".formatted(localeId));

    ShopCategory category = new ShopCategory(manager, title, rows, backgroundItem, backgroundSlots);

    ConfigurationSection itemsSection = section.getConfigurationSection("items");
    if (itemsSection == null)
      throw new DeserializationException("Failed to locate items section");

    for (String key : itemsSection.getKeys(false)) {
      ConfigurationSection itemSection = itemsSection.getConfigurationSection(key);
      //noinspection DataFlowIssue
      int slot = Integer.parseInt(itemSection.getName());
      category.addGuiItem(AbstractGuiItem.deserialize(platform, currency, slot, itemSection, itemFormatter, locale));
    }

    return category;
  }

  private final ShopManager manager;

  private final Component title;
  private final int rows;
  private final ItemBuilder background;
  private final int[] backgroundSlots;
  private final Set<AbstractGuiItem> items;

  public ShopCategory(ShopManager manager, Component title, int rows, ItemBuilder background, int[] backgroundSlots) {
    this.manager = manager;
    this.title = title;
    this.rows = rows;
    this.background = background;
    this.backgroundSlots = backgroundSlots;
    this.items = new HashSet<>();
  }

  public void addGuiItem(AbstractGuiItem item) {
    items.add(item);
  }

  private @NotNull GuiItem<Player, ItemStack> getBackground() {
    ItemStack background = this.background.build();
    background.editMeta(meta -> meta.displayName(Component.space()));
    return dev.triumphteam.gui.paper.builder.item.ItemBuilder.from(background).asGuiItem();
  }

  public void drawGui(@NotNull GuiContainer<@NotNull Player, @NotNull ItemStack> container, Player player) {
    GuiItem<Player, ItemStack> background = getBackground();
    for (int slot : getBackgroundSlots())
      container.setItem(slot, background);

    for (AbstractGuiItem item : getItems())
      container.setItem(item.slot(), item.asGuiItem(player));
  }

}
