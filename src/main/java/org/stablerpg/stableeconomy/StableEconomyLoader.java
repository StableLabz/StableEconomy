package org.stablerpg.stableeconomy;

import io.papermc.paper.plugin.loader.PluginClasspathBuilder;
import io.papermc.paper.plugin.loader.PluginLoader;
import io.papermc.paper.plugin.loader.library.impl.MavenLibraryResolver;
import org.eclipse.aether.artifact.DefaultArtifact;
import org.eclipse.aether.graph.Dependency;
import org.eclipse.aether.repository.RemoteRepository;
import org.jetbrains.annotations.NotNull;

import java.util.List;

@SuppressWarnings({"UnstableApiUsage", "unused"})
public final class StableEconomyLoader implements PluginLoader {

  private static final List<String> LIBRARIES = List.of(
    "com.zaxxer:HikariCP:7.0.1",
    "org.xerial:sqlite-jdbc:3.50.3.0",
    "com.h2database:h2:2.3.232",
    "org.mariadb.jdbc:mariadb-java-client:3.5.5",
    "org.postgresql:postgresql:42.7.7",
    "org.mongodb:mongodb-driver-sync:5.5.1"
  );

  @Override
  public void classloader(@NotNull PluginClasspathBuilder classpathBuilder) {
    MavenLibraryResolver resolver = new MavenLibraryResolver();
    resolver.addRepository(new RemoteRepository.Builder("central", "default", MavenLibraryResolver.MAVEN_CENTRAL_DEFAULT_MIRROR).build());
    LIBRARIES.stream().map(StableEconomyLoader::dependency).forEach(resolver::addDependency);
    classpathBuilder.addLibrary(resolver);
  }

  private static Dependency dependency(String dependency) {
    return new Dependency(new DefaultArtifact(dependency), null);

  }
}
