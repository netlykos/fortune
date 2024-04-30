package org.netlykos.fortune.utilities;

public class Utility {

  private Utility() {
    /** do nothing constructor */
  }

  public static boolean isPopulated(String s) {
    return s != null && !s.isBlank();
  }

}
