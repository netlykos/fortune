package org.netlykos.fortune;

import org.netlykos.fortune.file.service.FileFortuneManagerService;

public class LaunchLocalApp {

  public static void main(String[] args) {
    System.setProperty(FileFortuneManagerService.FILE_FORTUNE_MANAGER_SERVICE_FORTUNE_DIRECTORY_PROPERTY_NAME,
        "app/test-support/src/main/resources/file-library/success/fortune");
    App.main(args);
  }

}
