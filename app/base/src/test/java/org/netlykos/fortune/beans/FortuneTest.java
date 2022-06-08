package org.netlykos.fortune.beans;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.Arrays;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.Test;

class FortuneTest {

  private static final Logger LOGGER = LogManager.getLogger(FortuneTest.class);

  @Test
  void test() {
    String category = "category";
    Integer number = 10;
    List<String> lines = Arrays.asList("line 1", "line 2");
    Fortune bean = new Fortune(category, number, lines);
    LOGGER.debug(bean);
    assertNotNull(bean);
    assertFalse(bean.toString().isEmpty());
  }

}
