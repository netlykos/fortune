package org.netlykos.fortune.utilities;

public class XMLUtilities {

  private XMLUtilities() {
    /** do nothing constructor */
  }

  /**
   * Encode the characters '&lt;', '&gt;', '&amp;' to their escaped form.
   *
   * @param line
   * @return
   */
  public static String escapeXmlCharacters(String line) {
    return line
        .replace("&", "&amp;")
        .replace("<", "&lt;")
        .replace(">", "&gt;");
  }

}

