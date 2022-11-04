package sonia.scm.webhook;

import sonia.scm.plugin.ExtensionPoint;

@ExtensionPoint
public interface WebHookSpecification<T extends SingleWebHookConfiguration> {
  Class<T> getSpecificationType();
}
