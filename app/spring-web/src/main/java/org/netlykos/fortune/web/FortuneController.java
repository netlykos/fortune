package org.netlykos.fortune.web;

import static io.swagger.v3.oas.annotations.enums.ParameterIn.PATH;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.http.MediaType.APPLICATION_NDJSON_VALUE;
import static org.springframework.http.MediaType.APPLICATION_XHTML_XML_VALUE;
import static org.springframework.http.MediaType.APPLICATION_XML_VALUE;
import static org.springframework.http.MediaType.TEXT_HTML_VALUE;
import static org.springframework.http.MediaType.TEXT_PLAIN_VALUE;
import static org.springframework.http.MediaType.TEXT_XML_VALUE;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.netlykos.fortune.beans.Fortune;
import org.netlykos.fortune.beans.FortuneCategory;
import org.netlykos.fortune.service.FortuneManagerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.PostConstruct;
import reactor.core.publisher.Mono;


@RestController
@RequestMapping("/api/mvc")
@Tag(name = "fortune", description = "fortune cookie APIs")
public class FortuneController {

  private static final Logger LOGGER = LogManager.getLogger(FortuneController.class);
  private static final String TAB = "\t";
  private static final String EXPANDED_TAB = "    ";

  @Autowired
  List<FortuneManagerService> fortuneManagerServices;

  FortuneManagerService fortuneManagerService;

  @PostConstruct
  public void init() {
    LOGGER.debug("Injected with {}", fortuneManagerServices);
    if (fortuneManagerServices.isEmpty()) {
      throw new IllegalStateException("Expected at least one instance of a fortune manager to be available for use.");
    }
    this.fortuneManagerService = fortuneManagerServices.get(0);
    LOGGER.info("Selected: {}", this.fortuneManagerService);
  }

  // @formatter:off
  @Operation(
    summary = "Return a fortune cookie to the caller.",
    description = """
        Return a fortune cookie to the caller based on the parameters provided. If the parameter {cookie} is not
        populated, a random cookie from the supplied {category} is returned. If the parameter {category} is not
        populated a random category is selected and a random cookie from the category is returned.
        """,
    parameters = {
      @Parameter(name = "category", in = PATH, description = "A category as identified by the <i><u>/categories</u></i> endpoint", required = false),
      @Parameter(name = "cookie", in = PATH, description = "A cookie number in the range of the category", required = false)
    },
    responses = {
      @ApiResponse(responseCode = "200", description = "Successful operation"),
      @ApiResponse(responseCode = "404", description = "Invalid input", content = @Content)
    }
  )
  @GetMapping(
      path = { "/fortune", "/fortune/{category}", "/fortune/{category}/{cookie:[\\d]+}" },
      produces = {
         APPLICATION_XML_VALUE, TEXT_XML_VALUE,
         APPLICATION_JSON_VALUE, APPLICATION_NDJSON_VALUE,
         APPLICATION_XHTML_XML_VALUE, TEXT_HTML_VALUE,
         TEXT_PLAIN_VALUE
      })
  public Mono<Fortune> fortune(
      @PathVariable(name = "category", required = false) String category,
      @PathVariable(name = "cookie", required = false) Integer cookie
  ) {
  // @formatter:on
    Fortune fortune = getCookie(category, cookie);
    // need to replace "\t" <tabs> with space else the client gets "\t" in their response (in json)
    List<String> lines = fortune.lines();
    List<String> newLines = lines.stream().map(s -> s.replace(TAB, EXPANDED_TAB)).collect(Collectors.toList());
    return Mono.just(new Fortune(fortune.category(), fortune.number(), newLines));
  }

  // @formatter:off
  @GetMapping(path = "/categories",
    produces = {
         APPLICATION_XML_VALUE, TEXT_XML_VALUE,
         APPLICATION_JSON_VALUE, APPLICATION_NDJSON_VALUE,
         APPLICATION_XHTML_XML_VALUE, TEXT_HTML_VALUE,
         TEXT_PLAIN_VALUE
    })
  // @formatter:on
  public Mono<Collection<FortuneCategory>> categories() {
    return Mono.just(fortuneManagerService.getFortuneCategories());
  }

  private Fortune getCookie(String category, Integer cookie) {
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
