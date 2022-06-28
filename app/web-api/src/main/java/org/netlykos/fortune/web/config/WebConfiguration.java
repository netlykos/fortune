package org.netlykos.fortune.web.config;

import org.netlykos.fortune.web.codec.FortuneEncoder;
import org.springdoc.core.GroupedOpenApi;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.codec.ServerCodecConfigurer;
import org.springframework.web.reactive.config.WebFluxConfigurer;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;

@Configuration
public class WebConfiguration {

  @Value("${org.netlykos.fortune.webConfiguration.title:Fortune}")
  String title;

  @Value("${org.netlykos.fortune.webConfiguration.description:Fortune application Web API's specification}")
  String description;

  @Value("${org.netlykos.fortune.webConfiguration.license:BSD license}")
  String license;

  @Value("${org.netlykos.fortune.webConfiguration.url:http://routecvt01.netlykos.org/}")
  String url;

  @Bean
  WebFluxConfigurer webFluxConfigurer() {
    return new WebFluxConfigurer() {
      @Override
      public void configureHttpMessageCodecs(ServerCodecConfigurer configurer) {
        configurer.customCodecs().register(new FortuneEncoder());
      }
    };
  }

  @Bean
  public OpenAPI springShopOpenAPI() {
    return new OpenAPI()
        .info(new Info()
            .title(title)
            .description(description)
            .version("v0.0.1")
            .license(new License()
                .name(license)
                .url(url)));
  }

  @Bean
  public GroupedOpenApi publicApi() {
    return GroupedOpenApi.builder()
        .group("fortune")
        .pathsToMatch("/api/**")
        .build();
  }

}
