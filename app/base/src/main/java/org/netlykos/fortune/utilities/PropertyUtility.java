package org.netlykos.fortune.utilities;

import static org.netlykos.fortune.utilities.Utility.isPopulated;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class PropertyUtility {

  private static final Logger LOGGER = LogManager.getLogger(PropertyUtility.class);

  private PropertyUtility() {
    /** do nothing constructor */
  }

  public static String getPropertyValueOrDefault(String propertyName, String defaultValue) {
    String value = getPropertyValue(propertyName);
    return isPopulated(value) ? value : defaultValue;
  }

  public static String getPropertyValue(String propertyName) {
    LOGGER.trace("Looking up value for Property {}", propertyName);
    String value = System.getenv(propertyName);
    if (isPopulated(value)) {
      LOGGER.debug("Returning value [{}] for property [{}] from environment variable.", value, propertyName);
      return value;
    }
    value = System.getProperty(propertyName);
    if (isPopulated(value)) {
      LOGGER.debug("Returning value [{}] for property [{}] from system variable.", value, propertyName);
      return value;
    }
    return null;
  }

}
