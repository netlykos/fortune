package org.netlykos.fortune.beans;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.Test;

class FortuneCategoryTest {

  private static final Logger LOGGER = LogManager.getLogger(FortuneCategoryTest.class);

  @Test
  void test() {
    String category = "category";
    Integer totalRecords = 100;
    FortuneCategory bean = new FortuneCategory(category, totalRecords);
    LOGGER.debug(bean);
    assertNotNull(bean);
    assertFalse(bean.toString().isEmpty());
  }

}
