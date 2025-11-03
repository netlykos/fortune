package org.netlykos.fortune.spring.webmvc;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.netlykos.fortune.file.service.FileFortuneManagerService.FILE_FORTUNE_MANAGER_SERVICE_FORTUNE_DIRECTORY_PROPERTY_NAME;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.http.MediaType.APPLICATION_NDJSON_VALUE;
import static org.springframework.http.MediaType.APPLICATION_XHTML_XML_VALUE;
import static org.springframework.http.MediaType.APPLICATION_XML_VALUE;
import static org.springframework.http.MediaType.TEXT_HTML_VALUE;
import static org.springframework.http.MediaType.TEXT_PLAIN_VALUE;
import static org.springframework.http.MediaType.TEXT_XML_VALUE;

import java.util.Arrays;
import java.util.Collections;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

@SpringBootTest(webEnvironment = RANDOM_PORT)
class FortuneMvcControllerTest {

  private static final Logger LOGGER = LogManager.getLogger(FortuneMvcControllerTest.class);

  @Autowired
  private FortuneMvcController fortuneMvcController;

  @LocalServerPort
  private int port;

  @Autowired
  private TestRestTemplate restTemplate;

  @BeforeAll
  static void setup() {
    System.setProperty(FILE_FORTUNE_MANAGER_SERVICE_FORTUNE_DIRECTORY_PROPERTY_NAME,
        "../test-support/src/main/resources/file-library/success/fortune");
  }

  static void teardown() {
    System.clearProperty(FILE_FORTUNE_MANAGER_SERVICE_FORTUNE_DIRECTORY_PROPERTY_NAME);
  }

  @Test
  void contextLoads() {
    assertNotNull(fortuneMvcController);
  }

  @Test
  void testCategories() {
    String url = getUrl("/categories");
    LOGGER.debug("URL: {}", url);
    Arrays.asList(APPLICATION_XML_VALUE, TEXT_XML_VALUE,
        APPLICATION_JSON_VALUE, APPLICATION_NDJSON_VALUE,
        APPLICATION_XHTML_XML_VALUE, TEXT_HTML_VALUE,
        TEXT_PLAIN_VALUE).stream().forEach(mediaType -> {
          LOGGER.debug("Media Type: {}", mediaType);
          HttpHeaders headers = new HttpHeaders();
          headers.setAccept(Collections.singletonList(MediaType.valueOf(mediaType)));
          HttpEntity<String> requestEntity = new HttpEntity<>("", headers);
          ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, requestEntity, String.class);
          LOGGER.info("For media type: {} got response code {}, with body [{}]", mediaType, response.getStatusCode(), response.getBody());
          assertNotNull(response);
        });
  }

  @Test
  void testFortune() {

  }

  private String getUrl(String contextPath) {
    return "http://localhost:%d%s%s".formatted(port, FortuneMvcController.API_ENDPOINT, contextPath);
  }

}
