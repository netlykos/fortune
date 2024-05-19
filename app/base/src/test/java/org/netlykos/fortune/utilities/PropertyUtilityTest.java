package org.netlykos.fortune.utilities;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.netlykos.fortune.utilities.PropertyUtility.*;

import java.util.Map.Entry;

import java.util.Optional;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

class PropertyUtilityTest {

  private static final String FORTUNE_PROPERTY_UTILITY_TEST_KEY = "FORTUNE_PROPERTY_UTILITY_TEST_KEY";
  private static final String FORTUNE_PROPERTY_UTILITY_TEST_VALUE = "FORTUNE_PROPERTY_UTILITY_TEST_VALUE";

  @AfterEach
  void afterEach() {
    System.clearProperty(FORTUNE_PROPERTY_UTILITY_TEST_KEY);
  }

  @Test
  void testGetPropertyValue() {
    System.getenv().entrySet().stream().forEach( e -> System.out.println("%s: %s".formatted(e.getKey(), e.getValue())));
    Optional<Entry<String, String>> entry = System.getenv().entrySet().stream().findFirst();
    assertTrue(entry.isPresent());
    assertEquals(entry.get().getValue(), getPropertyValue(entry.get().getKey()));

    System.setProperty(FORTUNE_PROPERTY_UTILITY_TEST_KEY, FORTUNE_PROPERTY_UTILITY_TEST_VALUE);
    assertEquals(FORTUNE_PROPERTY_UTILITY_TEST_VALUE, getPropertyValue(FORTUNE_PROPERTY_UTILITY_TEST_KEY));

    assertNull(System.getProperty(FORTUNE_PROPERTY_UTILITY_TEST_VALUE));
  }

  @Test
  void testGetPropertyValueOrDefault() {
    var defaultValue = "default value";
    System.setProperty(FORTUNE_PROPERTY_UTILITY_TEST_KEY, FORTUNE_PROPERTY_UTILITY_TEST_VALUE);
    assertEquals(FORTUNE_PROPERTY_UTILITY_TEST_VALUE, getPropertyValueOrDefault(FORTUNE_PROPERTY_UTILITY_TEST_KEY, defaultValue));

    assertEquals(defaultValue, getPropertyValueOrDefault(FORTUNE_PROPERTY_UTILITY_TEST_VALUE, defaultValue));
  }

}
