package org.netlykos.fortune.utilities;

import java.util.Collection;

public class Utility {

  private Utility() {
    /** do nothing constructor */
  }

  public static boolean isPopulated(String s) {
    return s != null && !s.isBlank();
  }

  public static boolean isNull(Object ... objects) {
    if (objects == null) {
      return true;
    }
    for (Object obj : objects) {
      if (obj == null) {
        return true;
      }
    }
    return false;
  }

  public static void notNull(Object object, String message, Object... messageArgs) {
    if (object == null) {
      throw new IllegalArgumentException(message.formatted(messageArgs));
    }
  }

  public static <T> void notNull(Collection<T> collection, String message, Object ... messageArgs) {
    if (collection == null || collection.isEmpty()) {
      throw new IllegalArgumentException(message.formatted(messageArgs));
    }
  }

}
