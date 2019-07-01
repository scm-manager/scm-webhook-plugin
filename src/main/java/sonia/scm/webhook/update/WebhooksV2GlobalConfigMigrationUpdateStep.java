package sonia.scm.webhook.update;

import sonia.scm.migration.UpdateStep;
import sonia.scm.plugin.Extension;
import sonia.scm.store.ConfigurationStoreFactory;
import sonia.scm.version.Version;
import sonia.scm.webhook.WebHook;
import sonia.scm.webhook.WebHookConfiguration;

import javax.inject.Inject;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import static sonia.scm.version.Version.parse;

@Extension
public class WebhooksV2GlobalConfigMigrationUpdateStep implements UpdateStep {

  public static final String STORE_NAME = "webhook";
  private final ConfigurationStoreFactory storeFactory;

  @Inject
  public WebhooksV2GlobalConfigMigrationUpdateStep(ConfigurationStoreFactory storeFactory) {
    this.storeFactory = storeFactory;
  }

  @Override
  public void doUpdate() {
    Optional<V1WebhookConfiguration> optionalConfig = storeFactory.withType(V1WebhookConfiguration.class).withName(STORE_NAME).build().getOptional();
    if (isV1Config(optionalConfig)) {
      optionalConfig.ifPresent(
        v1WebhookConfiguration -> {
          Set<WebHook> v2Webhooks = new HashSet<>();
          v2Webhooks.iterator().forEachRemaining(webHook -> v2Webhooks.add(new WebHook(webHook.getUrlPattern(),webHook.isExecuteOnEveryCommit(), webHook.isSendCommitData(), webHook.getMethod())));
          WebHookConfiguration v2WebhookConfig = new WebHookConfiguration(v2Webhooks);

          storeFactory.withType(WebHookConfiguration.class).withName(STORE_NAME).build().set(v2WebhookConfig);
        }
      );
    }
  }

  private boolean isV1Config(Optional<V1WebhookConfiguration> optionalConfig) {
    if (optionalConfig.isPresent()) {
      try {
        return optionalConfig.get() instanceof V1WebhookConfiguration;
      } catch (ClassCastException e) {
        return true;
      }
    } else {
      return false;
    }
  }

  @Override
  public Version getTargetVersion() {
    return parse("2.0.0");
  }

  @Override
  public String getAffectedDataType() {
    return "sonia.scm.webhook.config.global.xml";
  }

  @XmlRootElement(name = "webhooks")
  @XmlAccessorType(XmlAccessType.FIELD)
  static class V1WebhookConfiguration extends WebHookConfiguration {
  }
}
