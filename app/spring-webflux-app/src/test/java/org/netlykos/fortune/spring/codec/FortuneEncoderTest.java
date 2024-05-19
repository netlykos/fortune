package org.netlykos.fortune.spring.codec;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.netlykos.fortune.beans.Fortune;
import org.springframework.core.ResolvableType;
import org.springframework.util.MimeTypeUtils;

class FortuneEncoderTest {

  private FortuneEncoder encoder = new FortuneEncoder();

  @Test
  void testCanEncode() {
    assertFalse(encoder.canEncode(null , MimeTypeUtils.ALL));
    assertFalse(encoder.canEncode(ResolvableType.forInstance(new Object()) , MimeTypeUtils.ALL));
    assertTrue(encoder.canEncode(ResolvableType.forClass(Fortune.class) , MimeTypeUtils.ALL));
  }

  @Test
  void testEncode() {
  }

  @Test
  void testEncodeValue() {
  }

  @Test
  void testGetContent() {
  }

  @Test
  void testGetEncodableMimeTypes() {
  }

}
