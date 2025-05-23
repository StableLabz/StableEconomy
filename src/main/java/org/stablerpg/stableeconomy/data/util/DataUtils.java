package org.stablerpg.stableeconomy.data.util;

import java.nio.ByteBuffer;
import java.util.UUID;

public final class DataUtils {

  public static byte[] uuidToBytes(UUID uuid) {
    ByteBuffer buffer = ByteBuffer.allocate(16);
    buffer.putLong(uuid.getMostSignificantBits());
    buffer.putLong(uuid.getLeastSignificantBits());
    return buffer.array();
  }

  public static UUID uuidFromBytes(byte[] bytes) {
    ByteBuffer buffer = ByteBuffer.wrap(bytes);
    return new UUID(buffer.getLong(), buffer.getLong());
  }

  private DataUtils() {
    throw new UnsupportedOperationException("This class cannot be instantiated");
  }

}
