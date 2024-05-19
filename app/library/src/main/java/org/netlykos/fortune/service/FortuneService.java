package org.netlykos.fortune.service;

import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.netlykos.fortune.beans.Fortune;
import org.netlykos.fortune.service.FortuneManagerService;

import jakarta.annotation.PostConstruct;
import jakarta.inject.Inject;
import jakarta.inject.Named;

@Named
public class FortuneService {

  private static final Logger LOGGER = LogManager.getLogger(FortuneService.class);

  @Inject
  List<FortuneManagerService> fortuneManagerServices;
  private FortuneManagerService fortuneManagerService;

  @PostConstruct
  void init() {
    LOGGER.debug("Injected with {}", fortuneManagerServices);
    if (fortuneManagerServices.isEmpty()) {
      throw new IllegalStateException("Expected at least one instance of a fortune manager to be available for use.");
    }
    this.fortuneManagerService = fortuneManagerServices.get(0);
    LOGGER.info("Selected: {}", this.fortuneManagerService);
  }

  public Fortune getFortune(String category, Integer cookie) {
    if (category != null) {
      if (cookie != null) {
        return fortuneManagerService.getFortune(category, cookie);
      }
      // we got a category, but no cookie number - select a random cookie from the category
      return fortuneManagerService.getRandomFortuneFromCategory(category);
    }
    return fortuneManagerService.getRandomFortune();
  }

}

