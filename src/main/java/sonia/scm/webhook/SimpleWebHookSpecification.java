package sonia.scm.webhook;

import sonia.scm.plugin.Extension;

@Extension
public class SimpleWebHookSpecification implements WebHookSpecification {
  @Override
  public Class<SimpleWebHook> getSpecificationType() {
    return SimpleWebHook.class;
  }
}
