package org.netlykos.fortune.exception;

/**
 * @author @netlykos (Adi B)
 *
 * This exception is thrown when a particular fortune cannot be found.
 */
public class FortuneNotFoundException extends IllegalArgumentException {

  public FortuneNotFoundException(String s) {
    this(s, null);
  }

  public FortuneNotFoundException(String message, Throwable cause) {
    super(message, cause);
  }

  public FortuneNotFoundException(String message, Throwable cause, Object ... messageArguments) {
    this(message.formatted(messageArguments), cause);
  }

}
