package org.stablerpg.stableeconomy.hooks;

import java.io.Closeable;

public interface Hook extends Closeable {

  boolean canLoad();

  void load();

  @Override
  void close();

}
