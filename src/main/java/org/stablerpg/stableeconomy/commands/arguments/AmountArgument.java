package org.stablerpg.stableeconomy.commands.arguments;

import dev.jorel.commandapi.arguments.ArgumentSuggestions;
import dev.jorel.commandapi.arguments.CustomArgument;
import dev.jorel.commandapi.arguments.StringArgument;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.stablerpg.stableeconomy.currency.formatting.Formatters;

public class AmountArgument extends CustomArgument<Double, String> {

  public AmountArgument() {
    this("amount");
  }

  public AmountArgument(String nodeName) {
    super(new StringArgument(nodeName), info -> {
      String input = info.currentInput();
      return Formatters.unformat(input)
        .orElseThrow(() -> CustomArgumentException.fromString("Invalid amount \"%s\"".formatted(input)));
    });
    replaceSuggestions(ArgumentSuggestions.strings(ignored -> Bukkit.getOnlinePlayers().stream().map(Player::getName).toArray(String[]::new)));
  }

}