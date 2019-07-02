package sonia.scm.webhook.update;

import com.google.common.collect.ImmutableMap;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sonia.scm.update.V1PropertyDaoTestUtil;
import sonia.scm.webhook.HttpMethod;
import sonia.scm.webhook.WebHook;
import sonia.scm.webhook.WebHookConfiguration;
import sonia.scm.webhook.WebHookContext;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class WebhooksV2ConfigMigrationUpdateStepTest {

  V1PropertyDaoTestUtil testUtil = new V1PropertyDaoTestUtil();

  @Mock
  WebHookContext context;

  @Captor
  ArgumentCaptor<WebHookConfiguration> globalConfigurationCaptor;
  @Captor
  ArgumentCaptor<String> repositoryIdCaptor;
  private WebhooksV2ConfigMigrationUpdateStep updateStep;

  @BeforeEach
  void captureStoreCalls() {
    lenient().doNothing().when(context).setRepositoryConfiguration(globalConfigurationCaptor.capture(), repositoryIdCaptor.capture());
  }

  @BeforeEach
  void initUpdateStep() {
    updateStep = new WebhooksV2ConfigMigrationUpdateStep(testUtil.getPropertyDAO(), context);
  }

  @Test
  void shouldMigrateRepositoryConfig() {
    Map<String, String> mockedValues =
      ImmutableMap.of(
        "webhooks", "http://example.com/${repositoryName};true;true;POST|"
      );

    testUtil.mockRepositoryProperties(new V1PropertyDaoTestUtil.PropertiesForRepository("repo", mockedValues));

    updateStep.doUpdate();

    verify(context).setRepositoryConfiguration(any(), eq("repo"));

    WebHook v2Webhook = new WebHook("http://example.com/${repositoryName}", true, true, HttpMethod.POST);

    assertThat(globalConfigurationCaptor.getValue().getWebhooks().contains(v2Webhook)).isTrue();
  }

  @Test
  void shouldSkipRepositoriesWithoutWebhookConfig() {
    Map<String, String> mockedValues =
      ImmutableMap.of(
        "any", "value"
      );

    testUtil.mockRepositoryProperties(new V1PropertyDaoTestUtil.PropertiesForRepository("repo", mockedValues));

    updateStep.doUpdate();

    verify(context, never()).setRepositoryConfiguration(any(), eq("repo"));
  }
}

