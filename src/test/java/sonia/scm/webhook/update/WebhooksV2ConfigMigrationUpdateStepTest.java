/*
 * MIT License
 *
 * Copyright (c) 2020-present Cloudogu GmbH and Contributors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package sonia.scm.webhook.update;

import com.google.common.collect.ImmutableMap;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import sonia.scm.store.ConfigurationStore;
import sonia.scm.store.InMemoryConfigurationStoreFactory;
import sonia.scm.update.V1PropertyDaoTestUtil;
import sonia.scm.webhook.DefaultWebHook;
import sonia.scm.webhook.HttpMethod;
import sonia.scm.webhook.WebHook;
import sonia.scm.webhook.WebHookConfiguration;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class WebhooksV2ConfigMigrationUpdateStepTest {

  private final static String REPO_NAME = "repo";

  V1PropertyDaoTestUtil testUtil = new V1PropertyDaoTestUtil();

  ConfigurationStore<WebHookConfiguration> configStore;

  private WebhooksV2ConfigMigrationUpdateStep updateStep;

  private InMemoryConfigurationStoreFactory storeFactory = new InMemoryConfigurationStoreFactory();

  @BeforeEach
  void initSetup() {
    configStore = storeFactory.withType(WebHookConfiguration.class).withName("webhook").forRepository(REPO_NAME).build();
    updateStep = new WebhooksV2ConfigMigrationUpdateStep(testUtil.getPropertyDAO(), storeFactory);
  }

  @Test
  void shouldMigrateRepositoryConfig() {
    Map<String, String> mockedValues =
      ImmutableMap.of(
        "webhooks", "http://example.com/${repositoryName};true;true;POST|"
      );
    testUtil.mockRepositoryProperties(new V1PropertyDaoTestUtil.PropertiesForRepository(REPO_NAME, mockedValues));

    updateStep.doUpdate();

    WebHook v2Webhook = new DefaultWebHook("http://example.com/${repositoryName}", true, true, HttpMethod.POST);

    assertThat(configStore.get().getWebhooks().contains(v2Webhook)).isTrue();
  }

  @Test
  void shouldMigrateRepositoryConfigWithMultipleWebhooks() {
    Map<String, String> mockedValues =
      ImmutableMap.of(
        "webhooks", "http://example.com/${repositoryName};true;true;POST|http://example.com/${zweiterWebhook};undefined;true;AUTO|http://example.com/${dritteWebhook};false;false;PUT|"
      );
    testUtil.mockRepositoryProperties(new V1PropertyDaoTestUtil.PropertiesForRepository(REPO_NAME, mockedValues));

    updateStep.doUpdate();

    WebHook v2Webhook1 = new DefaultWebHook("http://example.com/${repositoryName}", true, true, HttpMethod.POST);
    WebHook v2Webhook2 = new DefaultWebHook("http://example.com/${zweiterWebhook}", false, true, HttpMethod.AUTO);
    WebHook v2Webhook3 = new DefaultWebHook("http://example.com/${dritteWebhook}", false, false, HttpMethod.PUT);

    assertThat(configStore.get().getWebhooks().contains(v2Webhook1)).isTrue();
    assertThat(configStore.get().getWebhooks().contains(v2Webhook2)).isTrue();
    assertThat(configStore.get().getWebhooks().contains(v2Webhook3)).isTrue();
    assertThat(configStore.get().getWebhooks().size()).isEqualTo(3);
  }

  @Test
  void shouldSkipRepositoriesIfWebhookIsEmpty() {
    Map<String, String> mockedValues =
      ImmutableMap.of(
        "webhooks", ""
      );
    testUtil.mockRepositoryProperties(new V1PropertyDaoTestUtil.PropertiesForRepository(REPO_NAME, mockedValues));

    updateStep.doUpdate();

    assertThat(configStore.get()).isNull();
  }

  @Test
  void shouldMigrateRepositoryConfigIfWebhookMissesMethod() {
    Map<String, String> mockedValues =
      ImmutableMap.of(
        "webhooks", "http://example.com/${repositoryName};true;true|"
      );
    testUtil.mockRepositoryProperties(new V1PropertyDaoTestUtil.PropertiesForRepository(REPO_NAME, mockedValues));

    updateStep.doUpdate();

    WebHook v2Webhook = new DefaultWebHook("http://example.com/${repositoryName}", true, true, HttpMethod.AUTO);

    assertThat(configStore.get().getWebhooks().contains(v2Webhook)).isTrue();
  }

  @Test
  void shouldSkipRepositoriesWithoutWebhookConfig() {
    Map<String, String> mockedValues =
      ImmutableMap.of(
        "any", "value"
      );
    testUtil.mockRepositoryProperties(new V1PropertyDaoTestUtil.PropertiesForRepository(REPO_NAME, mockedValues));

    updateStep.doUpdate();

    assertThat(configStore.get()).isNull();
  }
}

