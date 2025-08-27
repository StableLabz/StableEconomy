package org.stablerpg.stableeconomy.data.databases;

import com.destroystokyo.paper.profile.PlayerProfile;
import com.google.common.base.Preconditions;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.jetbrains.annotations.NotNull;
import org.stablerpg.stableeconomy.EconomyPlatform;
import org.stablerpg.stableeconomy.config.database.DatabaseConfig;
import org.stablerpg.stableeconomy.data.PlayerAccount;
import org.stablerpg.stableeconomy.service.AbstractDatabaseService;

import java.util.*;
import java.util.concurrent.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.logging.Level;

public abstract class Database extends AbstractDatabaseService implements Listener {

  public static @NotNull Database of(@NotNull EconomyPlatform platform) {
    return switch (platform.getConfig().getDatabaseInfo().getDatabaseType()) {
      case SQLITE -> new SQLite(platform);
      case H2 -> new H2(platform);
      case MYSQL, MARIADB -> new MariaDB(platform);
      case POSTGRESQL -> new PostgreSQL(platform);
      case MONGODB -> new MongoDB(platform);
    };
  }

  protected Set<PlayerAccount> entries;
  protected Map<UUID, PlayerAccount> entriesByUUID;
  protected Map<String, PlayerAccount> entriesByUsername;
  private ScheduledExecutorService scheduler;
  private ScheduledFuture<?> autoSaveTask;

  protected Database(@NotNull EconomyPlatform platform) {
    super(platform);
  }

  protected void setup() {
    scheduler = Executors.newSingleThreadScheduledExecutor();
    int initialCapacity = lookupEntryCount() * 2;
    entries = new HashSet<>(initialCapacity);
    entriesByUUID = new HashMap<>(initialCapacity);
    entriesByUsername = new HashMap<>(initialCapacity);
    load();
    Bukkit.getPluginManager().registerEvents(this, getPlatform().getPlugin());
    long autoSaveInterval = getConfig().getDatabaseInfo().getAutoSaveInterval();
    autoSaveTask = getScheduler().scheduleAtFixedRate(this::save, autoSaveInterval, autoSaveInterval, TimeUnit.SECONDS);
  }

  protected int lookupEntryCount() {
    return Bukkit.getOfflinePlayers().length;
  }

  protected final DatabaseConfig getConfig() {
    return getPlatform().getConfig();
  }

  protected final ScheduledExecutorService getScheduler() {
    return scheduler;
  }

  public final void createOrUpdateUsername(@NotNull PlayerProfile profile) {
  }

  public final void createOrUpdateUsername(@NotNull UUID id, @NotNull String username) {
    Preconditions.checkNotNull(id, "UUID cannot be null");
    Preconditions.checkNotNull(username, "Username cannot be null");
    getAccount(id).thenAccept(account -> {
      if (account != null) {
        account.updateUsername(username);
        entriesByUsername.entrySet().removeIf(e -> e.getValue().equals(account));
        entriesByUsername.put(username, account);
      } else add(new PlayerAccount(platform, id, username));
    });
  }

  public final CompletableFuture<PlayerAccount> getAccount(@NotNull UUID uniqueId) {
    Preconditions.checkNotNull(uniqueId, "UUID cannot be null");
    return CompletableFuture.supplyAsync(() -> entriesByUUID.get(uniqueId), getScheduler());
  }

  public final void add(final @NotNull PlayerAccount playerAccount) {
    getScheduler().execute(() -> {
      entries.add(playerAccount);
      entriesByUUID.put(playerAccount.getUniqueId(), playerAccount);
      entriesByUsername.put(playerAccount.getUsername(), playerAccount);
    });
  }

  public final <R> CompletableFuture<R> query(@NotNull UUID uniqueId, @NotNull Function<PlayerAccount, R> query) {
    return getAccount(uniqueId).thenApply(account -> {
      Preconditions.checkNotNull(account, "Player account not found for '%s'", uniqueId);
      return query.apply(account);
    });
  }

  public final <R> CompletableFuture<R> query(@NotNull String username, @NotNull Function<PlayerAccount, R> query) {
    return getAccount(username).thenApply(account -> {
      Preconditions.checkNotNull(account, "Player account not found for '%s'", username);
      return query.apply(account);
    });
  }

  public final CompletableFuture<PlayerAccount> getAccount(@NotNull String username) {
    Preconditions.checkNotNull(username, "Username cannot be null");
    return CompletableFuture.supplyAsync(() -> entriesByUsername.get(username), getScheduler());
  }

  public final CompletableFuture<Void> update(@NotNull UUID uniqueId, @NotNull Consumer<PlayerAccount> consumer) {
    return getAccount(uniqueId).thenAccept(account -> {
      Preconditions.checkNotNull(account, "Player account not found for '%s'", uniqueId);
      consumer.accept(account);
    });
  }

  public final CompletableFuture<Void> update(@NotNull String username, @NotNull Consumer<PlayerAccount> consumer) {
    return getAccount(username).thenAccept(account -> {
      Preconditions.checkNotNull(account, "Player account not found for '%s'", username);
      consumer.accept(account);
    });
  }

  public final List<PlayerAccount> sortedByBalance(String currency) {
    List<PlayerAccount> leaderboard = new ArrayList<>(entries);
    leaderboard.sort(Comparator.comparing(account -> account.getBalanceEntry(currency), Comparator.reverseOrder()));
    return leaderboard;
  }

  @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
  public final void onPlayerLoginEvent(AsyncPlayerPreLoginEvent event) {
    createOrUpdateUsername(event.getPlayerProfile());
  }

  @Override
  public void close() {
    AsyncPlayerPreLoginEvent.getHandlerList().unregister(this);
    autoSaveTask.cancel(false);
    save();

    scheduler.shutdown();
    try {
      if (!scheduler.awaitTermination(5, TimeUnit.SECONDS)) scheduler.shutdownNow();
    } catch (InterruptedException exception) {
      platform.getLogger().log(Level.SEVERE, "Failed to shutdown scheduler", exception);
      scheduler.shutdownNow();
    }
    scheduler = null;

    entries = null;
    entriesByUUID = null;
    entriesByUsername = null;
  }

}
