package org.netlykos;

import static org.netlykos.fortune.file.service.FileFortuneManagerService.FILE_FORTUNE_MANAGER_SERVICE_FORTUNE_DIRECTORY_PROPERTY_NAME;

public class LaunchLocalApp {

  public static void main(String[] args) {
    String location = "app/test-support/src/main/resources/file-library/success/fortune";
    System.setProperty(FILE_FORTUNE_MANAGER_SERVICE_FORTUNE_DIRECTORY_PROPERTY_NAME, location);
    App.main(args);
  }

}
