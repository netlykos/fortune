package org.netlykos;

public class LaunchLocalApp {

  public static void main(String [] args) {
    String[] arguments = {
      "--org.netlykos.fortune.fileFortuneManagerService.directory=app/test-support/src/main/resources/file-library/success/fortune"
    };
    App.main(arguments);
  }

}
