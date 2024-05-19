package org.netlykos.fortune.utilities;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.netlykos.fortune.utilities.Utility.isNull;
import static org.netlykos.fortune.utilities.Utility.isPopulated;
import static org.netlykos.fortune.utilities.Utility.notNull;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.Test;

class UtilityTest {

  @Test
  void testIsNull() {
    assertTrue(isNull((Object[])null));
    assertTrue(isNull("value", null));
    assertFalse(isNull("value", "entry"));
  }

  @Test
  void testIsPopulated() {
    assertFalse(isPopulated(null));
    assertFalse(isPopulated(""));
    assertFalse(isPopulated(" "));
    assertTrue(isPopulated("value"));
    assertTrue(isPopulated(" value "));
  }

  @Test
  void testNotNull() {
    assertDoesNotThrow(() -> notNull("value", "message [%s]", "throws"));
    assertThrows(IllegalArgumentException.class, () -> notNull(null, "message [%s]", "throws"));
  }

  @Test
  void testNotNullCollection() {
    assertDoesNotThrow(() -> notNull(Arrays.asList("entry"), "message [%s]", "throws"));
    assertThrows(IllegalArgumentException.class, () -> notNull(null, "message [%s]", "throws"));
    List<String> list = Collections.emptyList();
    assertThrows(IllegalArgumentException.class, () -> notNull(list, "message [%s]", "throws"));
  }
}
