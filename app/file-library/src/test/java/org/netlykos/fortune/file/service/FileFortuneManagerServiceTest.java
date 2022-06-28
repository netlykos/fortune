package org.netlykos.fortune.file.service;

import static java.lang.String.format;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Stream;

import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.netlykos.fortune.beans.Fortune;
import org.netlykos.fortune.beans.FortuneCategory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(classes = FileFortuneManagerService.class, properties = {
    "org.netlykos.fortune.fileFortuneManagerService.directory=../test-support/src/main/resources/file-library/success/fortune" })
@TestMethodOrder(OrderAnnotation.class)
class FileFortuneManagerServiceTest {

  private static final Logger LOGGER = LoggerFactory.getLogger(FileFortuneManagerServiceTest.class);

  private String defaultCategory = "art";

  @Autowired
  FileFortuneManagerService fortuneManagerService;

  @Test
  @Order(1)
  void init() {
    assertNotNull(fortuneManagerService);
  }

  @Test
  void testGetRandomFortune() {
    Fortune fortune = fortuneManagerService.getRandomFortune();
    LOGGER.debug("{}", fortune);
    assertNotNull(fortune);
  }

  @Test
  void testGetRandomFortuneFromCategory() {
    Fortune fortune = fortuneManagerService.getRandomFortuneFromCategory(defaultCategory);
    LOGGER.debug("{}", fortune);
    assertNotNull(fortune);
  }

  @Test
  void testGetRandomFortuneFromCategoryFailure() {
    String category = "unknown";
    String expected = String.format("No fortunes for category [%s] available.", category);
    IllegalArgumentException actual = assertThrows(IllegalArgumentException.class, () -> {
      fortuneManagerService.getRandomFortuneFromCategory(category);
    });
    assertEquals(expected, actual.getMessage());
  }

  @Test
  void testGetFortuneCategory() {
    FortuneCategory fortuneCategory = fortuneManagerService.getFortuneCategory(defaultCategory);
    LOGGER.debug("{}", fortuneCategory);
    assertNotNull(fortuneCategory);
    assertEquals(defaultCategory, fortuneCategory.category());
    assertTrue(fortuneCategory.totalRecords() > 1);
  }

  @Test
  void testGetFortuneCategoryFailure() {
    String category = "unknown";
    String expected = String.format("Category %s is not setup.", category);
    IllegalArgumentException actual = assertThrows(IllegalArgumentException.class, () -> {
      fortuneManagerService.getFortuneCategory(category);
    });
    assertEquals(expected, actual.getMessage());
  }

  @Test
  @Order(2)
  void testGetFortuneCategories() {
    Collection<FortuneCategory> fortuneCategories = fortuneManagerService.getFortuneCategories();
    assertTrue(fortuneCategories.size() > 0);
    assertTrue(fortuneCategories.stream().anyMatch(e -> e.category().equals(defaultCategory)));
  }

  @Test
  void testGetFortuneSuccess() {
    int cookie = 3;
    List<String> expect = Arrays.asList("A celebrity is a person who is known for his well-knownness.");
    Fortune actual = fortuneManagerService.getFortune(defaultCategory, cookie);
    LOGGER.debug("{}", actual);
    assertEquals(expect.size(), actual.lines().size());
    assertEquals(expect.get(0), actual.lines().get(0));
  }

  @Test
  void testGetFortuneSuccessEdge() {
    int cookie = 465;
    List<String> expect = Arrays.asList(
        "\"Hiro has two loves, baseball and porn, but due to an elbow injury he",
        "gives up baseball....\"",
        "  -- AniDB description of _H2_, with selective quoting applied.",
        "     http://anidb.info/perl-bin/animedb.pl?show=anime&aid=352");
    Fortune actual = fortuneManagerService.getFortune(defaultCategory, cookie);
    LOGGER.debug("{}", actual);
    assertEquals(expect.size(), actual.lines().size());
    for (int i = 0; i < expect.size(); i++) {
      assertEquals(expect.get(i), actual.lines().get(i));
    }
  }

  @Test
  void testGetFortuneFailureBadCategory() {
    String category = "not_a_valid_category";
    String expected = String.format("Category %s is not setup.", category);
    IllegalArgumentException actual = assertThrows(IllegalArgumentException.class, () -> {
      fortuneManagerService.getFortune(category, 1);
    });
    assertEquals(expected, actual.getMessage());
  }

  @Test
  void testGetFortuneFailureNegativeCookie() {
    int cookie = -1;
    String expected = "Cookie number should be positive.";
    IllegalArgumentException actual = assertThrows(IllegalArgumentException.class, () -> {
      fortuneManagerService.getFortune(defaultCategory, cookie);
    });
    assertEquals(expected, actual.getMessage());
  }

  @Test
  void testGetFortuneFailureOverflowCookie() {
    int range = 465, cookie = range + 1;
    String expected = String.format("Category %s only contains %d cookie(s).", defaultCategory, range);
    IllegalArgumentException actual = assertThrows(IllegalArgumentException.class, () -> {
      fortuneManagerService.getFortune(defaultCategory, cookie);
    });
    assertEquals(expected, actual.getMessage());
  }

  @Test
  void testGetResourceContent() {
  }

  @Test
  void testFileFortuneManagerServiceBadFortuneDirectory() {
    String directory = "some/unknown/directory";
    String expected = format("Failed to find any resource at path [%s]", directory);
    FileFortuneManagerService fileFortuneManagerService = new FileFortuneManagerService();
    fileFortuneManagerService.fortuneDirectory = directory;
    IllegalArgumentException actual = assertThrows(IllegalArgumentException.class, fileFortuneManagerService::init);
    assertEquals(expected, actual.getMessage());
  }

  @Test
  void testFileFortuneManagerServiceBadDirectoryContents() {
    String directory = "../test-support/src/main/resources/file-library/failure/struct-data-but-no-content";
    String expected = format("Failed to find any resource at path [%s/%s]", directory, defaultCategory);
    FileFortuneManagerService fileFortuneManagerService = new FileFortuneManagerService();
    fileFortuneManagerService.fortuneDirectory = directory;
    IllegalArgumentException actual = assertThrows(IllegalArgumentException.class, fileFortuneManagerService::init);
    LOGGER.debug(actual.getMessage(), actual);
    assertEquals(expected, actual.getMessage());
  }

  @Test
  void testFileFortuneManagerServicedInvalidDirectoryContents() {
    String directory = "../test-support/src/main/resources/file-library/failure/struct-data-invalid-content";
    FileFortuneManagerService fileFortuneManagerService = new FileFortuneManagerService();
    fileFortuneManagerService.fortuneDirectory = directory;
    fileFortuneManagerService.init();
    Collection<FortuneCategory> fortuneCategories = fileFortuneManagerService.getFortuneCategories();
    LOGGER.debug("{}", fortuneCategories);
    assertTrue(fortuneCategories.isEmpty());
  }

  @Test
  void testIsValidFortuneFiles() {
    byte[] dataFileContent = new byte[10];
    byte[] structFileContent = new byte[30];
    assertTrue(FileFortuneManagerService.isValidFortuneFiles(dataFileContent, structFileContent));
  }

  @ParameterizedTest()
  @MethodSource("provideValidFortuneFilesFailureParameters")
  void testIsValidFortuneFilesFailures(byte[] dataFileContent, byte[] structFileContent) {
    assertFalse(FileFortuneManagerService.isValidFortuneFiles(dataFileContent, structFileContent));
  }

  private static Stream<Arguments> provideValidFortuneFilesFailureParameters() {
    return Stream.of(
      Arguments.of(null, null),
      Arguments.of(new byte[10], null),
      Arguments.of(new byte[0], new byte[30]),
      Arguments.of(new byte[10], new byte[23])
    );
  }

}
