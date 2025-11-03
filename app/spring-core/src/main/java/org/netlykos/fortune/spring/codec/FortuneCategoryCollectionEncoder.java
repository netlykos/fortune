package org.netlykos.fortune.spring.codec;

import static org.netlykos.fortune.utilities.Utility.notNull;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.http.MediaType.APPLICATION_XHTML_XML_VALUE;
import static org.springframework.http.MediaType.APPLICATION_XML_VALUE;
import static org.springframework.http.MediaType.TEXT_HTML_VALUE;
import static org.springframework.http.MediaType.TEXT_PLAIN_VALUE;
import static org.springframework.http.MediaType.TEXT_XML_VALUE;

import java.util.Collection;
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

public class FortuneCategoryCollectionEncoder extends BaseEncoder implements Encoder<Collection<FortuneCategory>> {

  private static final Logger LOGGER = LogManager.getLogger(FortuneCategoryCollectionEncoder.class);

  @Override
  public boolean canEncode(ResolvableType elementType, MimeType mimeType) {
    LOGGER.debug("canEncode invoked with {}, {}", elementType, mimeType);
    if (!canEncode(Collection.class, elementType, mimeType)) {
      return false;
    }
    if (!elementType.hasGenerics()) {
      return false;
    }
    ResolvableType genericResolvableType = elementType.getGeneric(0);
    LOGGER.trace("Generic type for collection is {}", genericResolvableType);
    return FortuneCategory.class.isAssignableFrom(genericResolvableType.toClass());
  }

  @Override
  public Flux<DataBuffer> encode(Publisher<? extends Collection<FortuneCategory>> inputStream,
      DataBufferFactory bufferFactory, ResolvableType elementType, MimeType mimeType, Map<String, Object> hints) {
    LOGGER.debug("encode invoked with {}, {}, {}, {}", inputStream, elementType, mimeType, hints);
    canEncodeCollection(inputStream, bufferFactory, elementType);
    if (inputStream instanceof Mono) {
      return Mono.from(inputStream)
          .map(value -> encodeValue(value, bufferFactory, elementType, mimeType, hints))
          .flux();
    }
    throw new UnsupportedOperationException("Unable to support input stream of type %s".formatted(inputStream));
  }

  @Override
  public DataBuffer encodeValue(Collection<FortuneCategory> value, DataBufferFactory bufferFactory,
      ResolvableType valueType, MimeType mimeType, Map<String, Object> hints) {
    LOGGER.debug("encodeValue invoked with {}, {}, {}, {}", value, valueType, mimeType, hints);
    byte[] content = getContent(value, mimeType.toString()).getBytes();
    DataBuffer buffer = bufferFactory.allocateBuffer(content.length);
    buffer.write(content);
    return buffer;
  }

  @Override
  public List<MimeType> getEncodableMimeTypes() {
    return BaseEncoder.encodableMimeTypes;
  }

  static <T> void canEncodeCollection(Publisher<? extends Collection<? extends T>> inputStream,
      DataBufferFactory bufferFactory, ResolvableType elementType) {
    LOGGER.debug("encode invoked with {}, {}, {}", inputStream, bufferFactory, elementType);
    notNull(inputStream, "'inputStream' must not be null");
    notNull(bufferFactory, "'bufferFactory' must not be null");
    notNull(elementType, "'elementType' must not be null");
  }

  static String getContent(Collection<FortuneCategory> fortuneCategories, String mimeType) {
    return switch (mimeType) {
      case APPLICATION_XML_VALUE, TEXT_XML_VALUE -> getXml(fortuneCategories);
      case APPLICATION_JSON_VALUE -> getJson(fortuneCategories);
      case APPLICATION_XHTML_XML_VALUE, TEXT_HTML_VALUE -> getHtmlFragment(fortuneCategories);
      case TEXT_PLAIN_VALUE -> getText(fortuneCategories);
      default -> throw new UnsupportedOperationException("Unable to encode fortune category collection as %s"
          .formatted(mimeType));
    };
  }

  static String getText(Collection<FortuneCategory> fortuneCategories) {
    StringBuilder sb = new StringBuilder();
    for (FortuneCategory fortuneCategory : fortuneCategories) {
      sb.append(FortuneCategoryEncoder.getText(fortuneCategory)).append(NEW_LINE);
    }
    return sb.toString();
  }

  static String getXml(Collection<FortuneCategory> fortuneCategories) {
    StringBuilder sb = new StringBuilder()
        .append(XML_PREAMBLE)
        .append("<fortuneCategories>");
    for (FortuneCategory fortuneCategory : fortuneCategories) {
      sb.append(FortuneCategoryEncoder.getXml(fortuneCategory));
    }
    sb.append("</fortuneCategories>");
    return sb.toString();
  }

  static String getJson(Collection<FortuneCategory> fortuneCategories) {
    StringBuilder sb = new StringBuilder().append("[");
    sb.append(FortuneCategoryEncoder.getJson(fortuneCategories.iterator().next()));
    for (int i = 1; i < fortuneCategories.size(); i++) {
      sb.append(", ");
      sb.append(FortuneCategoryEncoder.getJson(fortuneCategories.iterator().next()));
    }
    sb.append("]");
    return sb.toString();
  }

  static String getHtmlFragment(Collection<FortuneCategory> fortuneCategories) {
    StringBuilder sb = new StringBuilder();
    sb.append("<div>");
    for (FortuneCategory fortuneCategory : fortuneCategories) {
      sb.append(FortuneCategoryEncoder.getHtmlFragment(fortuneCategory));
    }
    sb.append("</div>");
    return sb.toString();
  }

}
