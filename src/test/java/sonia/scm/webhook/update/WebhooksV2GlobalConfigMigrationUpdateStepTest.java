package sonia.scm.webhook.update;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import sonia.scm.store.ConfigurationStore;
import sonia.scm.store.InMemoryConfigurationStoreFactory;
import sonia.scm.webhook.HttpMethod;
import sonia.scm.webhook.WebHook;
import sonia.scm.webhook.WebHookConfiguration;
import sonia.scm.webhook.update.WebhooksV2GlobalConfigMigrationUpdateStep.V1WebhookConfiguration;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class WebhooksV2GlobalConfigMigrationUpdateStepTest {

  WebhooksV2GlobalConfigMigrationUpdateStep updateStep;
  private static final String WEBHOOK_STORE_NAME = "webhooks";

  private InMemoryConfigurationStoreFactory storeFactory = new InMemoryConfigurationStoreFactory();

  @BeforeEach
  void initUpdateStep() {
    updateStep = new WebhooksV2GlobalConfigMigrationUpdateStep(storeFactory);
  }

  @Nested
  class WithExistingV1Config {

    @BeforeEach
    void createWebhookV1XMLInMemory() {
      V1WebhookConfiguration webhooksConfiguration = new V1WebhookConfiguration();
      WebHook v1WebHook = new WebHook("abc",true,true, HttpMethod.AUTO);
      webhooksConfiguration.getWebhooks().add(v1WebHook);
      storeFactory.withType(V1WebhookConfiguration.class).withName(WEBHOOK_STORE_NAME).build().set(webhooksConfiguration);
    }

    @Test
    void shouldMigrateGlobalConfiguration() {
      updateStep.doUpdate();
      ConfigurationStore<WebHookConfiguration> testStore = storeFactory.get(WEBHOOK_STORE_NAME, null);
      WebHookConfiguration webhookConfiguration = testStore.get();
      WebHook v2Webhook = webhookConfiguration.getWebhooks().iterator().next();
      assertThat(v2Webhook.getUrlPattern()).isEqualToIgnoringCase("abc");
      assertThat(v2Webhook.getMethod()).isEqualTo(HttpMethod.AUTO);
      assertThat(v2Webhook.isExecuteOnEveryCommit()).isTrue();
      assertThat(v2Webhook.isSendCommitData()).isTrue();
    }
  }

  @Nested
  class WithExistingV2Config {
    @BeforeEach
    void createWebhookV2XMLInMemory() {
      WebHookConfiguration globalConfiguration = new WebHookConfiguration();
      storeFactory.withType(WebHookConfiguration.class).withName(WEBHOOK_STORE_NAME).build().set(globalConfiguration);
    }

    @Test
    void shouldNotFailForExistingV2Config() {
      updateStep.doUpdate();
    }
  }

  @Nested
  class WithoutAnyConfig {
    @BeforeEach
    void createWebhookV2XMLInMemory() {
    }

    @Test
    void shouldNotFailForMissingConfig() {
      updateStep.doUpdate();
    }
  }
}
