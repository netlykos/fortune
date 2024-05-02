package org.netlykos.fortune.web.config;

import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.http.MediaType.APPLICATION_XHTML_XML;
import static org.springframework.http.MediaType.APPLICATION_XML;
import static org.springframework.http.MediaType.TEXT_HTML;
import static org.springframework.http.MediaType.TEXT_PLAIN;
import static org.springframework.http.HttpStatus.*;
import static org.springframework.http.MediaType.*;
import static org.springframework.web.reactive.function.server.ServerResponse.*;
import static org.springframework.web.reactive.function.server.RequestPredicates.*;
import static org.springframework.web.reactive.function.server.RouterFunctions.*;

import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.netlykos.fortune.beans.Fortune;
import org.netlykos.fortune.web.codec.FortuneEncoder;
import org.netlykos.fortune.web.service.FortuneService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerCodecConfigurer;
import org.springframework.web.reactive.config.WebFluxConfigurer;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;

import reactor.core.publisher.Mono;

@Configuration
public class WebConfiguration {

  private static final Logger LOGGER = LogManager.getLogger(WebConfiguration.class);

  public static final String HTTP_HEADER_X_REASON = "x-failure-reason";
  public static final String HTTP_HEADER_X_FORTUNE_CATEGORY = "x-fortune-category";
  public static final String HTTP_HEADER_X_FORTUNE_COOKIE = "x-fortune-cookie";

  @Bean
  WebFluxConfigurer webFluxConfigurer() {
    LOGGER.debug("Registering WebFluxConfigurer.");
    return new WebFluxConfigurer() {
      @Override
      public void configureHttpMessageCodecs(ServerCodecConfigurer configurer) {
        configurer.customCodecs().register(new FortuneEncoder());
      }
    };
  }

  @Bean
  RouterFunction<ServerResponse> routes(FortuneService fortuneService) {
    return route()
        .path("/api/flux/fortune", b1 -> b1
            .nest(accept(MediaType.ALL), b2 -> b2
                .nest(contentType(fortuneProduces()), b3 -> b3
                    .GET("/{category}/{cookie:[\\d]+}", request -> processFortuneRequest(request, fortuneService))
                    .GET("/{category}", request -> processFortuneRequest(request, fortuneService))
                    .GET("/", request -> processFortuneRequest(request, fortuneService))
                    .GET("", request -> processFortuneRequest(request, fortuneService)))))
        .build();
  }

  private static Mono<ServerResponse> processFortuneRequest(ServerRequest request, FortuneService fortuneService) {
    Map<String, String> pathVariables = request.pathVariables();
    String category = pathVariables.getOrDefault("category", null);
    String cookieString = pathVariables.getOrDefault("cookie", null);
    Integer cookie = cookieString == null ? null : Integer.valueOf(cookieString);
    try {
      Fortune fortune = fortuneService.getFortune(category, cookie);
      return ok()
          .header(HTTP_HEADER_X_FORTUNE_CATEGORY, fortune.category())
          .header(HTTP_HEADER_X_FORTUNE_COOKIE, String.valueOf(fortune.number()))
          .bodyValue(fortune);
    } catch (IllegalArgumentException e) {
      String message = e.getMessage();
      LOGGER.warn(message, e);
      return status(NOT_FOUND)
          .header(HTTP_HEADER_X_REASON, message)
          .bodyValue(message);
    }
  }

  private static MediaType[] fortuneProduces() {
    return new MediaType[] { APPLICATION_XML, TEXT_XML, APPLICATION_JSON, APPLICATION_XHTML_XML, TEXT_HTML,
        TEXT_PLAIN };
  }

}
