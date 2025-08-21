package org.stablerpg.stableeconomy.gui.config;

import com.google.common.base.Preconditions;
import dev.triumphteam.gui.click.ClickContext;
import dev.triumphteam.gui.paper.Gui;
import dev.triumphteam.gui.paper.container.type.PaperContainerType;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Range;
import org.stablerpg.stableeconomy.config.BasicConfig;
import org.stablerpg.stableeconomy.gui.AbstractGui;
import org.stablerpg.stableeconomy.gui.AbstractGuiItem;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

public class ConfigGui extends AbstractGui<BasicConfig> {

  public static void open(@NotNull BasicConfig config, @NotNull Player player) {
    Preconditions.checkState(config.getClass().isAnnotationPresent(ConfigInventory.class));
    new ConfigGui(config).open(player);
  }

  private static PaperContainerType getContainerType(InventoryType inventoryType, @Range(from = 0, to = 6) int rows) {
    return switch (inventoryType) {
      case CHEST -> PaperContainerType.chest(rows);
      case HOPPER -> PaperContainerType.hopper();
      default -> throw new IllegalStateException("Unexpected value: " + inventoryType);
    };
  }

  private static Set<AbstractGuiItem> getConfigItems(BasicConfig config) {
    return Arrays.stream(config.getClass().getDeclaredFields())
      .filter(field -> field.isAnnotationPresent(ConfigItem.class))
      .map(field -> field.getAnnotation(ConfigItem.class))
      .map(configItem -> new AbstractGuiItem() {
        @Override
        public int slot() {
          return configItem.slot();
        }

        @Override
        public ItemStack build(Player player) {
          ItemStack item = ItemStack.of(Material.PAPER);
          Component displayName = MiniMessage.miniMessage().deserialize(configItem.displayName());
          item.editMeta(meta -> meta.displayName(displayName));
          return item;
        }

        @Override
        public void execute(Player player, ClickContext context) {
          player.sendRichMessage("<red>Not implemented yet!"); // TODO: Implement config item editing
        }
      })
      .collect(Collectors.toUnmodifiableSet());
  }

  protected ConfigGui(BasicConfig context) {
    super(context);
  }

  @Override
  protected Gui generate(@NotNull Player player, @NotNull BasicConfig config) {
    ConfigInventory configInventory = config.getClass().getAnnotation(ConfigInventory.class);
    Component title = MiniMessage.miniMessage().deserialize(configInventory.title());
    InventoryType inventoryType = configInventory.inventoryType();
    int rows = configInventory.rows();

    Preconditions.checkState(inventoryType == InventoryType.CHEST && rows != 0);
    PaperContainerType containerType = getContainerType(inventoryType, rows);
    return Gui.of(containerType)
      .title(title)
      .statelessComponent(container -> {
      }).build();
  }

}
