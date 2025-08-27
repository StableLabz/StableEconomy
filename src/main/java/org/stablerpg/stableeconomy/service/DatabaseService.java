package org.stablerpg.stableeconomy.service;

import com.destroystokyo.paper.profile.PlayerProfile;
import com.google.common.base.Preconditions;
import org.jetbrains.annotations.NotNull;
import org.stablerpg.stableeconomy.EconomyPlatform;
import org.stablerpg.stableeconomy.data.PlayerAccount;

import java.io.Closeable;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.function.Function;

public interface DatabaseService extends Service, Closeable {

  EconomyPlatform getPlatform();

  default void createOrUpdateUsername(@NotNull PlayerProfile profile) {
    Preconditions.checkNotNull(profile, "Player profile cannot be null");
    UUID id = profile.getId();
    Preconditions.checkNotNull(id, "Player profile UUID cannot be null");
    String name = profile.getName();
    Preconditions.checkNotNull(name, "Player profile username cannot be null");

    createOrUpdateUsername(id, name);
  }

  void createOrUpdateUsername(@NotNull UUID id, @NotNull String username);

  void add(@NotNull PlayerAccount account);

  CompletableFuture<PlayerAccount> getAccount(@NotNull UUID id);

  CompletableFuture<PlayerAccount> getAccount(@NotNull String username);

  <R> CompletableFuture<R> query(@NotNull UUID id, @NotNull Function<PlayerAccount, R> query);

  <R> CompletableFuture<R> query(@NotNull String username, @NotNull Function<PlayerAccount, R> query);

  CompletableFuture<Void> update(@NotNull UUID id, @NotNull Consumer<PlayerAccount> consumer);

  CompletableFuture<Void> update(@NotNull String username, @NotNull Consumer<PlayerAccount> consumer);

  List<PlayerAccount> sortedByBalance(String currency);

}
