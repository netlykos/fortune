package org.netlykos.fortune.base.web;

public class WebConstants {

  public static final String TAB = "\t";
  public static final String EXPANDED_TAB = "    ";
  public static final String EXAMPLE_APPLICATION_JSON = """
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
  public static final String EXAMPLE_APPLICATION_XML = """
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
  public static final String EXAMPLE_APPLICATION_XHTML = """
        <div>
          <p>Cookie number 503 selected from category science.
            <br />The University of California Statistics Department; where mean is normal,
            <br />and deviation standard.
            <br />
          </p>
        </div>
      """;
  public static final String EXAMPLE_TEXT_PLAIN = """
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

  private WebConstants() {
    /* do nothing */
  }

}
