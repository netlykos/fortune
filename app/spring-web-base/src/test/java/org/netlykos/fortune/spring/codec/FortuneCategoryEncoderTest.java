package org.netlykos.fortune.spring.codec;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.http.MediaType.APPLICATION_PDF;
import static org.springframework.util.MimeTypeUtils.APPLICATION_JSON_VALUE;
import static org.springframework.util.MimeTypeUtils.APPLICATION_XML_VALUE;
import static org.springframework.util.MimeTypeUtils.TEXT_HTML_VALUE;
import static org.springframework.util.MimeTypeUtils.TEXT_PLAIN_VALUE;

import java.nio.ByteBuffer;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.netlykos.fortune.beans.FortuneCategory;
import org.springframework.core.ResolvableType;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferFactory;
import org.springframework.core.io.buffer.DefaultDataBufferFactory;
import org.springframework.util.MimeType;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

class FortuneCategoryEncoderTest {

  private static final Logger LOGGER = LogManager.getLogger(FortuneCategoryEncoderTest.class);

  private FortuneCategoryEncoder encoder = new FortuneCategoryEncoder();
  private FortuneCategory fortuneCategory = new FortuneCategory("test", 42);
  private ResolvableType resolvableTypeFortuneCategory = ResolvableType.forClass(FortuneCategory.class);
  private DataBufferFactory dataBufferFactory;

  @BeforeEach
  void setUp() {
    dataBufferFactory = new DefaultDataBufferFactory();
  }

  @Test
  void testCanEncodeForAllTypes() {
    BaseEncoder.encodableMimeTypes.forEach(mimeType -> {
      assertTrue(encoder.canEncode(resolvableTypeFortuneCategory, mimeType), "Failed for MIME type: %s".formatted(mimeType));
    });
  }

  @Test
  void testCanEncodeFalse() {
    assertFalse(encoder.canEncode(null, APPLICATION_JSON));
    assertFalse(encoder.canEncode(resolvableTypeFortuneCategory, APPLICATION_PDF));
    assertFalse(encoder.canEncode(ResolvableType.forClass(String.class), APPLICATION_JSON));
  }

  @Test
  void testGetEncodableMimeTypesNotEmpty() {
    List<MimeType> mimeTypes = encoder.getEncodableMimeTypes();
    assertNotNull(mimeTypes);
    assertFalse(mimeTypes.isEmpty());
    assertEquals(BaseEncoder.encodableMimeTypes.size(), mimeTypes.size());
  }

  @Test
  void testEncodeMono() {
    Mono<FortuneCategory> mono = Mono.just(fortuneCategory);
    // now encode the given mono
    Flux<DataBuffer> fluxDataBuffer = encoder.encode(mono, dataBufferFactory, resolvableTypeFortuneCategory, APPLICATION_JSON, Map.of());
    assertNotNull(fluxDataBuffer);
    // Trigger the encoding
    List<DataBuffer> block = fluxDataBuffer.collectList().block();
    assertNotNull(block);
    assertFalse(block.isEmpty());
    assertEquals(1, block.size());
    DataBuffer dataBuffer = block.get(0);
    ByteBuffer byteBuffer = ByteBuffer.allocate(dataBuffer.readableByteCount());
    dataBuffer.toByteBuffer(byteBuffer);
    String content = new String(byteBuffer.array());
    LOGGER.debug("Encoded content: {}", content);
    assertEquals("{ \"category\": \"test\", \"totalRecords\": 42 }", content);
  }

  @Test
  void testEncodeUnsupportedPublisherThrows() {
    // assertThrows(UnsupportedOperationException.class, () -> {
    //   encoder.encode(Mono.empty().repeat(), bufferFactory, ResolvableType.forClass(FortuneCategory.class),
    //       MimeType.valueOf("application/json"), Map.of());
    // });
  }

  @Test
  void testEncodeValueReturnsBuffer() {
    DataBuffer buffer = encoder.encodeValue(fortuneCategory, dataBufferFactory, resolvableTypeFortuneCategory, APPLICATION_JSON, Map.of());
    assertNotNull(buffer);
  }


  @Test
  void testGetContentJson() {
    String result = FortuneCategoryEncoder.getContent(fortuneCategory, APPLICATION_JSON_VALUE);
    assertTrue(result.contains("\"category\": \"test\""));
    assertTrue(result.contains("\"totalRecords\": 42"));
  }

  @Test
  void testGetContentXml() {
    String result = FortuneCategoryEncoder.getContent(fortuneCategory, APPLICATION_XML_VALUE);
    assertTrue(result.contains("<?xml"));
    assertTrue(result.contains("fortuneCategory=\"test\""));
    assertTrue(result.contains("totalRecords=\"42\""));
  }

  @Test
  void testGetContentHtml() {
    String result = FortuneCategoryEncoder.getContent(fortuneCategory, TEXT_HTML_VALUE);
    assertTrue(result.contains("<div>"));
    assertTrue(result.contains("Fortune category test has a total of 42 records."));
  }

  @Test
  void testGetContentText() {
    String result = FortuneCategoryEncoder.getContent(fortuneCategory, TEXT_PLAIN_VALUE);
    assertTrue(result.contains("category=test"));
    assertTrue(result.contains("totalRecords=42"));
  }

  @Test
  void testGetContentUnsupportedThrows() {
    assertThrows(IllegalArgumentException.class, () -> {
      FortuneCategoryEncoder.getContent(fortuneCategory, "unsupported/type");
    });
  }

}
