package sonia.scm.webhook;

import sonia.scm.plugin.ExtensionPoint;

@ExtensionPoint
public interface WebHookSpecification {
  Class<? extends SingleWebHookConfiguration> getSpecificationType();
}
