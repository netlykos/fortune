package org.netlykos.fortune.web.config;

import org.netlykos.fortune.web.codec.FortuneEncoder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.codec.ServerCodecConfigurer;
import org.springframework.web.reactive.config.WebFluxConfigurer;

@Configuration
public class WebConfiguration {

  @Bean
  WebFluxConfigurer webFluxConfigurer() {
    return new WebFluxConfigurer() {
      @Override
      public void configureHttpMessageCodecs(ServerCodecConfigurer configurer) {
        configurer.customCodecs().register(new FortuneEncoder());
      }
    };
  }

}
