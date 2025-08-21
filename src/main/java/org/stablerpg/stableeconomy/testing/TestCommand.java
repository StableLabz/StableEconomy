package org.stablerpg.stableeconomy.testing;

import dev.jorel.commandapi.CommandAPI;
import dev.jorel.commandapi.CommandTree;
import dev.jorel.commandapi.arguments.IntegerArgument;
import dev.jorel.commandapi.arguments.StringArgument;
import lombok.SneakyThrows;
import org.stablerpg.stableeconomy.EconomyPlatform;
import org.stablerpg.stableeconomy.data.PlayerAccount;
import org.stablerpg.stableeconomy.data.databases.Database;

import java.lang.reflect.Field;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Function;

public class TestCommand {

  private static final ThreadLocalRandom random = ThreadLocalRandom.current();
  private static final Function<EconomyPlatform, CommandTree> command = platform -> new CommandTree("test")
    .then(new StringArgument("createPlayerAccounts")
      .then(new IntegerArgument("amount", 1)
        .executes((executor, args) -> {
          Integer amount = args.getByClass("amount", Integer.class);

          if (amount == null)
            throw CommandAPI.failWithString("Invalid amount specified.");

          for (int i = 0; i < amount; i++) {
            PlayerAccount account = generatePlayerAccount(platform);
            getDatabase(platform).add(account);
          }

          executor.sendRichMessage("<gray>Successfully created <yellow>" + amount + "</yellow> player accounts.</gray>");
        })));

  public static void registerCommand(EconomyPlatform platform) {
    command.apply(platform).register("lickmyballs");
  }

  @SneakyThrows
  private static Database getDatabase(EconomyPlatform platform) {
    Field databaseField = EconomyPlatform.class.getDeclaredField("database");
    databaseField.setAccessible(true);
    return (Database) databaseField.get(platform);
  }

  private static PlayerAccount generatePlayerAccount(EconomyPlatform platform) {
    UUID uuid = UUID.randomUUID();
    String username = "Player_" + uuid.toString().substring(0, 8);
    PlayerAccount account = new PlayerAccount(platform, uuid, username);
    account.setBalance("default", random.nextDouble(0, 1_000_000_000_000_000.0));
    return account;
  }

}
