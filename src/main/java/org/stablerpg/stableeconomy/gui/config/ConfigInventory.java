package org.stablerpg.stableeconomy.gui.config;

import org.bukkit.event.inventory.InventoryType;
import org.jetbrains.annotations.Range;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface ConfigInventory {

  String title();
  InventoryType inventoryType() default InventoryType.CHEST;
  @Range(from = 0, to = 6) int rows() default 0;

}
