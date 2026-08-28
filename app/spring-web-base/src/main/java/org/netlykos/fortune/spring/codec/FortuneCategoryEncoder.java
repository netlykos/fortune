package org.netlykos.fortune.spring.codec;

import static org.netlykos.fortune.utilities.Utility.notNull;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.http.MediaType.APPLICATION_XHTML_XML_VALUE;
import static org.springframework.http.MediaType.APPLICATION_XML_VALUE;
import static org.springframework.http.MediaType.TEXT_HTML_VALUE;
import static org.springframework.http.MediaType.TEXT_PLAIN_VALUE;
import static org.springframework.http.MediaType.TEXT_XML_VALUE;

import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.netlykos.fortune.beans.FortuneCategory;
import org.reactivestreams.Publisher;
import org.springframework.core.ResolvableType;
import org.springframework.core.codec.Encoder;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferFactory;
import org.springframework.util.MimeType;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public class FortuneCategoryEncoder extends BaseEncoder implements Encoder<FortuneCategory> {

  private static final Logger LOGGER = LogManager.getLogger(FortuneCategoryEncoder.class);

  @Override
  public boolean canEncode(ResolvableType elementType, MimeType mimeType) {
    return canEncode(FortuneCategory.class, elementType, mimeType);
  }

  @Override
  public Flux<DataBuffer> encode(Publisher<? extends FortuneCategory> inputStream, DataBufferFactory bufferFactory,
      ResolvableType elementType, MimeType mimeType, Map<String, Object> hints) {
    LOGGER.debug("encode invoked with {}, {}, {}, {}", inputStream, elementType, mimeType, hints);
    notNull(inputStream, "'inputStream' must not be null");
    isPopulated(bufferFactory, elementType, mimeType);
    if (inputStream instanceof Mono) {
      return Mono.from(inputStream)
          .map(value -> encodeValue(value, bufferFactory, elementType, mimeType, hints))
          .flux();
    }
    throw new UnsupportedOperationException("Unable to support input stream of type %s".formatted(inputStream));
  }

  @Override
  public DataBuffer encodeValue(FortuneCategory value, DataBufferFactory bufferFactory, ResolvableType valueType,
      MimeType mimeType, Map<String, Object> hints) {
    LOGGER.debug("encodeValue invoked with {}, {}, {}, {}", value, valueType, mimeType, hints);
    notNull(value, "'value' must not be null");
    isPopulated(bufferFactory, valueType, mimeType);
    byte[] content = getContent(value, mimeType.toString()).getBytes();
    DataBuffer buffer = bufferFactory.allocateBuffer(content.length);
    buffer.write(content);
    return buffer;
  }

  @Override
  public List<MimeType> getEncodableMimeTypes() {
    return BaseEncoder.encodableMimeTypes;
  }

  static String getContent(FortuneCategory fortune, String contentType) {
    return switch (contentType) {
      case APPLICATION_XML_VALUE, TEXT_XML_VALUE -> getXmlWithPreamble(fortune);
      case APPLICATION_XHTML_XML_VALUE, TEXT_HTML_VALUE -> getHtmlFragment(fortune);
      case APPLICATION_JSON_VALUE -> getJson(fortune);
      case TEXT_PLAIN_VALUE -> getText(fortune);
      default ->
        throw new IllegalArgumentException("Cannot create content-type [%s] for [%s]".formatted(contentType, fortune));
    };
  }

  static String getText(FortuneCategory fortuneCategory) {
    StringBuilder sb = new StringBuilder();
    sb.append("category=").append(fortuneCategory.category()).append(NEW_LINE);
    sb.append("totalRecords=").append(fortuneCategory.totalRecords()).append(NEW_LINE);
    return sb.toString();
  }

  static String getXmlWithPreamble(FortuneCategory fortuneCategory) {
    StringBuilder sb = new StringBuilder();
    sb.append(XML_PREAMBLE);
    sb.append(getXml(fortuneCategory));
    return sb.toString();
  }

  static String getXml(FortuneCategory fortuneCategory) {
    StringBuilder sb = new StringBuilder();
    sb.append("<fortuneCategory=\"%s\" totalRecords=\"%d\"></fortuneCategory>".formatted(fortuneCategory.category(),
        fortuneCategory.totalRecords()));
    return sb.toString();
  }

  static String getJson(FortuneCategory fortuneCategory) {
    return "{ \"category\": \"%s\", \"totalRecords\": %d }".formatted(fortuneCategory.category(),
        fortuneCategory.totalRecords());
  }

  static String getHtmlFragment(FortuneCategory fortuneCategory) {
    StringBuilder sb = new StringBuilder();
    sb.append("<div>");
    sb.append("<p>");
    sb.append("Fortune category %s has a total of %d records.".formatted(fortuneCategory.category(),
        fortuneCategory.totalRecords()));
    sb.append("</p>");
    sb.append("</div>");
    return sb.toString();
  }

}
