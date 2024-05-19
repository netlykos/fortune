package org.netlykos.fortune.spring.codec;

import static java.util.stream.Collectors.joining;
import static org.netlykos.fortune.utilities.Utility.isNull;
import static org.netlykos.fortune.utilities.Utility.notNull;
import static org.netlykos.fortune.utilities.XMLUtilities.escapeXmlCharacters;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.http.MediaType.APPLICATION_XHTML_XML;
import static org.springframework.http.MediaType.APPLICATION_XHTML_XML_VALUE;
import static org.springframework.http.MediaType.APPLICATION_XML;
import static org.springframework.http.MediaType.APPLICATION_XML_VALUE;
import static org.springframework.http.MediaType.TEXT_HTML;
import static org.springframework.http.MediaType.TEXT_HTML_VALUE;
import static org.springframework.http.MediaType.TEXT_PLAIN;
import static org.springframework.http.MediaType.TEXT_PLAIN_VALUE;
import static org.springframework.http.MediaType.TEXT_XML;
import static org.springframework.http.MediaType.TEXT_XML_VALUE;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.netlykos.fortune.beans.Fortune;
import org.netlykos.fortune.utilities.XMLUtilities;
import org.reactivestreams.Publisher;
import org.springframework.core.ResolvableType;
import org.springframework.core.codec.Encoder;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferFactory;
import org.springframework.util.MimeType;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public class FortuneEncoder implements Encoder<Fortune> {

  private static final Logger LOGGER = LogManager.getLogger(FortuneEncoder.class);
  private static final String NEW_LINE = System.getProperty("line.separator");
  private static final String BR = "<br />";
  private static final String XML_PREAMBLE = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>";

  private List<MimeType> encodableMimeTypes = Arrays.asList(
      APPLICATION_XML, TEXT_XML,
      APPLICATION_JSON,
      APPLICATION_XHTML_XML, TEXT_HTML,
      TEXT_PLAIN);

  @Override
  public boolean canEncode(ResolvableType elementType, MimeType mimeType) {
    LOGGER.debug("canEncode invoked with {}, {}", elementType, mimeType);
    if (isNull(elementType, mimeType)) {
      return false;
    }
    Class<?> cls = elementType.toClass();
    if (!Fortune.class.isAssignableFrom(cls)) {
      return false;
    }
    return this.encodableMimeTypes.stream().anyMatch(candidate -> candidate.isCompatibleWith(mimeType));
  }

  @Override
  public Flux<DataBuffer> encode(Publisher<? extends Fortune> inputStream, DataBufferFactory bufferFactory,
      ResolvableType elementType, MimeType mimeType, Map<String, Object> hints) {
    LOGGER.debug("encode invoked with {}, {}, {}, {}", inputStream, elementType, mimeType, hints);
    notNull(inputStream, "'inputStream' must not be null");
    notNull(bufferFactory, "'bufferFactory' must not be null");
    notNull(elementType, "'elementType' must not be null");
    if (inputStream instanceof Mono) {
      return Mono.from(inputStream)
          .map(value -> encodeValue(value, bufferFactory, elementType, mimeType, hints))
          .flux();
    }
    throw new UnsupportedOperationException("Unable to support input stream of type %s".formatted(inputStream));
  }

  @Override
  public DataBuffer encodeValue(Fortune value, DataBufferFactory bufferFactory, ResolvableType valueType,
      MimeType mimeType, Map<String, Object> hints) {
    LOGGER.debug("encodeValue invoked with {}, {}, {}, {}", value, valueType, mimeType, hints);
    byte[] content = getContent(value, mimeType.toString()).getBytes();
    DataBuffer buffer = bufferFactory.allocateBuffer(content.length);
    buffer.write(content);
    return buffer;
  }

  @Override
  public List<MimeType> getEncodableMimeTypes() {
    return this.encodableMimeTypes;
  }

  public static String getContent(Fortune fortune, String contentType) {
    return switch (contentType) {
      case APPLICATION_XML_VALUE, TEXT_XML_VALUE -> getXml(fortune);
      case APPLICATION_XHTML_XML_VALUE, TEXT_HTML_VALUE -> getHtmlFragment(fortune);
      case APPLICATION_JSON_VALUE -> getJson(fortune);
      case TEXT_PLAIN_VALUE -> getText(fortune);
      default ->
        throw new IllegalArgumentException("Cannot create content-type [%s] for [%s]".formatted(contentType, fortune));
    };
  }

  private static String getText(Fortune fortune) {
    StringBuilder sb = new StringBuilder()
        .append("category=").append(fortune.category()).append(NEW_LINE)
        .append("number=").append(fortune.number()).append(NEW_LINE);
    sb.append(fortune.lines().stream().collect(Collectors.joining(NEW_LINE)));
    return sb.toString();
  }

  private static String getXml(Fortune f) {
    String line = f.lines().stream().map(l -> "<line>%s</line>".formatted(escapeXmlCharacters(l))).collect(joining());
    StringBuilder sb = new StringBuilder()
        .append(XML_PREAMBLE)
        .append("<fortune category=\"%s\" number=\"%d\">".formatted(f.category(), f.number())).append("<lines>")
        .append(line)
        .append("</lines>").append("</fortune>");
    return sb.toString();
  }

  private static String getJson(Fortune f) {
    return """
          {
            "fortune": {
              "category": "%s",
              "number": %d,
              "lines": [
                %s
              ]
            }
          }
        """.formatted(
        f.category(),
        f.number(),
        f.lines().stream().map("\"%s\""::formatted).collect(joining(",", "      ", "")));
  }

  private static String getHtmlFragment(Fortune f) {
    String line = f.lines().stream().map(XMLUtilities::escapeXmlCharacters).collect(joining(BR, BR, BR));
    StringBuilder sb = new StringBuilder()
        .append("<div>")
        .append("<p>")
        .append("Cookie number %d selected from category %s.".formatted(f.number(), f.category()))
        .append(line)
        .append("</p>")
        .append("</div>");
    return sb.toString();
  }

}
