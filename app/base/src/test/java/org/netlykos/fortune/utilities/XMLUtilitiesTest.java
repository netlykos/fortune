package org.netlykos.fortune.utilities;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.netlykos.fortune.utilities.XMLUtilities.escapeXmlCharacters;

import org.junit.jupiter.api.Test;

class XMLUtilitiesTest {

  @Test
  void testEscapeXmlCharacters() {
    assertEquals("&amp;&amp;", escapeXmlCharacters("&&"));
    assertEquals("&lt;&lt;", escapeXmlCharacters("<<"));
    assertEquals("&gt;&gt;", escapeXmlCharacters(">>"));
    assertEquals("&amp;&lt;&gt;", escapeXmlCharacters("&<>"));
  }

}
