package org.netlykos.fortune.provider;

import org.netlykos.fortune.service.FortuneManagerService;

import java.util.ArrayList;
import java.util.List;
import java.util.ServiceLoader;

public final class FortuneManagerProvider {

  private FortuneManagerProvider() {
    /** do nothing constructor */
  }

  //All providers
  public static List<FortuneManagerService> providers() {
    List<FortuneManagerService> services = new ArrayList<>();
    ServiceLoader<FortuneManagerService> loader = ServiceLoader.load(FortuneManagerService.class);
    loader.forEach(services::add);
    return services;
  }

}

