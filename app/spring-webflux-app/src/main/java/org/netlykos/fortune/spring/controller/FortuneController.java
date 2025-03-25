package org.netlykos.fortune.spring.controller;

import static io.swagger.v3.oas.annotations.enums.ParameterIn.PATH;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
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
import org.netlykos.fortune.exception.FortuneNotFoundException;
import org.netlykos.fortune.provider.FortuneManagerProvider;
import org.netlykos.fortune.service.FortuneManagerService;
import org.netlykos.fortune.utilities.Utility;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.headers.Header;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.PostConstruct;

/**
 * @author @netlykos (Adi B)
 *
 *         This is a restful endpoint for the fortune controller.
 */
@RestController
@RequestMapping("/api")
@Tag(name = "fortune", description = "Fortune cookie APIs")
public class FortuneController {

  private static final Logger LOGGER = LogManager.getLogger(FortuneController.class);
  private static final String TAB = "\t";
  private static final String EXPANDED_TAB = "        ";
  private static final String EXAMPLE_APPLICATION_JSON = """
        {
          "category": "linuxcookie",
          "number": 3,
          "lines": [
            "Actually, typing random strings in the Finder does the equivalent of",
            "filename completion.",
            "(Discussion in comp.os.linux.misc on the intuitiveness of commands: file",
            "completion vs. the Mac Finder.)"
          ]
        }
      """;
  private static final String EXAMPLE_APPLICATION_XML = """
      <?xml version="1.0" encoding="UTF-8" standalone="yes"?>
      <fortune category="pratchett" number="2">
        <lines>
          <line>The Assassin moved quietly from roof to roof until he was well away from</line>
          <line>the excitement around the Watch House. His movements could be called</line>
          <line>cat-like, except that he did not stop to spray urine up against things.</line>
          <line>                -- Terry Pratchett, "Night Watch</line>
        </lines>
      </fortune>
      """;
  private static final String EXAMPLE_APPLICATION_XHTML = """
        <div>
          <p>Cookie number 503 selected from category science.
            <br />The University of California Statistics Department; where mean is normal,
            <br />and deviation standard.
            <br />
          </p>
        </div>
      """;
  private static final String EXAMPLE_TEXT_PLAIN = """
        category=knghtbrd
        number=529
        "Nvidia's OpenGL drivers are my "gold standard", and it has been quite a
        while since I have had to report a problem to them, and even their brand
        new extensions work as documented the first time I try them.  When I have
        a problem on an Nvidia, I assume that it is my fault.  With anyone else's
        drivers, I assume it is their fault.  This has turned out correct almost
        all the time."
                        -- John Carmack
      """;

  public static final String HTTP_HEADER_X_REASON = "x-failure-reason";
  public static final String HTTP_HEADER_X_FORTUNE_CATEGORY = "x-fortune-category";
  public static final String HTTP_HEADER_X_FORTUNE_COOKIE = "x-fortune-cookie";

  FortuneManagerService fortuneManagerService;

  @PostConstruct
  public void init() {
    List<FortuneManagerService> services = FortuneManagerProvider.providers();
    Utility.notNull(services, "FortuneManagerProvider could find no instances of FortuneManagerService.");
    fortuneManagerService = services.get(0);
    LOGGER.info("Selected: {}", this.fortuneManagerService);
  }

  @Operation(summary = "Return a fortune cookie to the caller.", description = """
      Return a fortune cookie to the caller based on the parameters provided. If the parameter {cookie} is not
      populated, a random cookie from the supplied {category} is returned. If the parameter {category} is not
      populated a random category is selected and a random cookie from the category is returned.
      """, parameters = {
      @Parameter(name = "category", in = PATH, description = "A category as identified by the <i><u>/categories</u></i> endpoint", required = false),
      @Parameter(name = "cookie", in = PATH, description = "A cookie number in the range of the category", required = false)
  }, responses = {
      @ApiResponse(responseCode = "200", description = "Successful operation", headers = {
          @Header(name = HTTP_HEADER_X_FORTUNE_CATEGORY, description = "The category for the fortune returned"),
          @Header(name = HTTP_HEADER_X_FORTUNE_COOKIE, description = "The number from the category for the fortune returned")
      }, content = {
          @Content(examples = @ExampleObject(name = "Json example", description = "An example of a JSON response ", value = EXAMPLE_APPLICATION_JSON), mediaType = APPLICATION_JSON_VALUE, schema = @Schema(implementation = Fortune.class)),
          @Content(examples = @ExampleObject(name = "Plain text example", description = "An example of a plain text response ", value = EXAMPLE_TEXT_PLAIN), mediaType = TEXT_PLAIN_VALUE, schema = @Schema(implementation = Fortune.class)),
          @Content(examples = @ExampleObject(name = "Text/HTML example", description = "An example of a Text/HTML response ", value = EXAMPLE_APPLICATION_XHTML), mediaType = TEXT_HTML_VALUE, schema = @Schema(implementation = Fortune.class)),
          @Content(examples = @ExampleObject(name = "Text/XML example", description = "An example of a Text/XML response ", value = EXAMPLE_APPLICATION_XML), mediaType = TEXT_XML_VALUE, schema = @Schema(implementation = Fortune.class)),
          @Content(examples = @ExampleObject(name = "XHTML example", description = "An example of a XHTML response ", value = EXAMPLE_APPLICATION_XHTML), mediaType = APPLICATION_XHTML_XML_VALUE, schema = @Schema(implementation = Fortune.class)),
          @Content(examples = @ExampleObject(name = "XML examples", description = "An example of a XML response", value = EXAMPLE_APPLICATION_XML), mediaType = APPLICATION_XML_VALUE, schema = @Schema(implementation = Fortune.class))
      }),
      @ApiResponse(responseCode = "404", description = "Returned when either the category is not found, or the combination of category and cookie is not found", content = @Content(), headers = @Header(name = HTTP_HEADER_X_REASON, description = "A textual reason that explains why the request failed."))
  })
  @GetMapping(path = { "/fortune", "/fortune/{category}", "/fortune/{category}/{cookie:[\\d]+}" }, produces = {
      APPLICATION_JSON_VALUE,
      APPLICATION_XML_VALUE, TEXT_XML_VALUE,
      APPLICATION_XHTML_XML_VALUE, TEXT_HTML_VALUE,
      TEXT_PLAIN_VALUE
  })
  public ResponseEntity<Fortune> fortune(
      @PathVariable(name = "category", required = false) String category,
      @PathVariable(name = "cookie", required = false) Integer cookie) {
    Fortune fortune = this.fortuneManagerService.getFortune(category, cookie);
    // need to replace "\t" <tabs> with space else the client gets "\t" in the
    // response
    List<String> lines = fortune.lines();
    List<String> newLines = lines.stream().map(s -> s.replace(TAB, EXPANDED_TAB)).toList();
    return ResponseEntity.ok()
        .header(HTTP_HEADER_X_FORTUNE_CATEGORY, fortune.category())
        .header(HTTP_HEADER_X_FORTUNE_COOKIE, String.valueOf(fortune.number()))
        .body(new Fortune(fortune.category(), fortune.number(), newLines));
  }

  @GetMapping(path = "/categories", produces = {
      APPLICATION_XML_VALUE, TEXT_XML_VALUE,
      APPLICATION_XHTML_XML_VALUE, TEXT_HTML_VALUE,
      APPLICATION_JSON_VALUE,
      TEXT_PLAIN_VALUE
  })
  public Collection<FortuneCategory> categories() {
    return fortuneManagerService.getFortuneCategories();
  }

  @ExceptionHandler
  public ResponseEntity<String> handle(FortuneNotFoundException e) {
    return ResponseEntity.notFound().header(HTTP_HEADER_X_REASON, e.getMessage()).build();
  }

}
