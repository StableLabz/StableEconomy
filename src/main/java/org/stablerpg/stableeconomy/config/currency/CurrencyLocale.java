package org.stablerpg.stableeconomy.config.currency;

import net.kyori.adventure.audience.Audience;
import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.NotNull;
import org.stablerpg.stableeconomy.config.exceptions.DeserializationException;
import org.stablerpg.stableeconomy.config.messages.AbstractLocale;
import org.stablerpg.stableeconomy.config.messages.messages.AbstractMessage;
import org.stablerpg.stableeconomy.config.messages.messages.Messages;

import java.util.HashMap;
import java.util.Map;

public final class CurrencyLocale extends AbstractLocale<CurrencyMessageType> {

  public static CurrencyLocale deserialize(ConfigurationSection section) throws DeserializationException {
    Map<CurrencyMessageType, AbstractMessage<?>> messages = new HashMap<>();
    for (CurrencyMessageType type : CurrencyMessageType.values()) {
      if (section.isString(type.getKey())) {
        messages.put(type, Messages.chat(section.getString(type.getKey())));
        continue;
      }
      ConfigurationSection messageSection = section.getConfigurationSection(type.getKey());

      if (messageSection == null)
        throw new DeserializationException("Missing message for " + type.getKey());

      messages.put(type, Messages.deserialize(messageSection));
    }

    return new CurrencyLocale(messages);
  }

  private CurrencyLocale(Map<CurrencyMessageType, AbstractMessage<?>> messages) {
    super(messages);
  }

  @Override
  public void sendParsedMessage(@NotNull Audience audience, @NotNull CurrencyMessageType id, @NotNull String... tags) {
    id.checkTags(tags);
    super.sendParsedMessage(audience, id, tags);
  }

}
