package org.netlykos.fortune.spring.config;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.netlykos.fortune.spring.codec.FortuneEncoder;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.codec.ServerCodecConfigurer;
import org.springframework.web.reactive.config.EnableWebFlux;
import org.springframework.web.reactive.config.WebFluxConfigurer;

/**
 * @author @netlykos (Adi B)
 *
 *         Configuration class to support controller.
 *
 */
@Configuration
@EnableWebFlux
public class WebConfiguration implements WebFluxConfigurer {

  private static final Logger LOGGER = LogManager.getLogger(WebConfiguration.class);

  @Override
  public void configureHttpMessageCodecs(ServerCodecConfigurer configurer) {
    LOGGER.debug("Registering WebFluxConfigurer.");
    configurer.customCodecs().register(new FortuneEncoder());
  }

}
