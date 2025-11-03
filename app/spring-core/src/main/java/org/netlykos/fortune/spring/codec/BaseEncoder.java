package org.netlykos.fortune.spring.codec;

import static java.util.Arrays.asList;
import static java.util.Collections.unmodifiableList;
import static org.netlykos.fortune.utilities.Utility.isNull;
import static org.netlykos.fortune.utilities.Utility.notNull;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.http.MediaType.APPLICATION_XHTML_XML;
import static org.springframework.http.MediaType.APPLICATION_XML;
import static org.springframework.http.MediaType.TEXT_HTML;
import static org.springframework.http.MediaType.TEXT_PLAIN;
import static org.springframework.http.MediaType.TEXT_XML;

import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.core.ResolvableType;
import org.springframework.core.io.buffer.DataBufferFactory;
import org.springframework.util.MimeType;

public class BaseEncoder {

  private static final Logger LOGGER = LogManager.getLogger(BaseEncoder.class);

  static final String NEW_LINE = System.getProperty("line.separator");
  static final String BR = "<br />";
  static final String XML_PREAMBLE = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>";

  public static final List<MimeType> encodableMimeTypes = unmodifiableList(
      asList(APPLICATION_XML, TEXT_XML, APPLICATION_JSON, APPLICATION_XHTML_XML, TEXT_HTML, TEXT_PLAIN));

  <T> boolean canEncode(Class<T> assignable, ResolvableType elementType, MimeType mimeType) {
    LOGGER.debug("canEncode invoked with {}, {}", elementType, mimeType);
    if (isNull(elementType, mimeType)) {
      return false;
    }
    Class<?> cls = elementType.toClass();
    if (!assignable.isAssignableFrom(cls)) {
      return false;
    }
    return encodableMimeTypes.stream().anyMatch(candidate -> candidate.isCompatibleWith(mimeType));
  }

  void isPopulated(DataBufferFactory bufferFactory, ResolvableType elementType, MimeType mimeType) {
    notNull(bufferFactory, "'bufferFactory' must not be null");
    notNull(elementType, "'elementType' must not be null");
    notNull(mimeType, "'mimeType' must not be null");
  }

  static String escapeJsonStrings(String s) {
    return s.replace("\"", "\\\"");
  }

}
