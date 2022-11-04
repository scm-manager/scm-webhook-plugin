package sonia.scm.webhook;

public class UnknownWebHookException extends RuntimeException {
  public UnknownWebHookException(String name) {
    super(name);
  }
}
