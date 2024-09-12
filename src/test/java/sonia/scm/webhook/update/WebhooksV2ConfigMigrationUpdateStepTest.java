/*
 * Copyright (c) 2020 - present Cloudogu GmbH
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU Affero General Public License as published by the Free
 * Software Foundation, version 3.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see https://www.gnu.org/licenses/.
 */

package sonia.scm.webhook.update;

import com.google.common.collect.ImmutableMap;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sonia.scm.security.KeyGenerator;
import sonia.scm.store.ConfigurationStore;
import sonia.scm.store.InMemoryConfigurationStoreFactory;
import sonia.scm.update.V1PropertyDaoTestUtil;
import sonia.scm.webhook.HttpMethod;
import sonia.scm.webhook.SimpleWebHook;
import sonia.scm.webhook.WebHookConfiguration;

import java.util.Map;

import static java.util.Collections.emptyList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class WebhooksV2ConfigMigrationUpdateStepTest {

  private final static String REPO_NAME = "repo";

  V1PropertyDaoTestUtil testUtil = new V1PropertyDaoTestUtil();

  ConfigurationStore<WebHookConfiguration> configStore;

  private WebhooksV2ConfigMigrationUpdateStep updateStep;

  private final InMemoryConfigurationStoreFactory storeFactory = new InMemoryConfigurationStoreFactory();

  @Mock
  private KeyGenerator keyGenerator;

  @BeforeEach
  void initSetup() {
    configStore = storeFactory.withType(WebHookConfiguration.class).withName("webhook").forRepository(REPO_NAME).build();
    updateStep = new WebhooksV2ConfigMigrationUpdateStep(testUtil.getPropertyDAO(), storeFactory, keyGenerator);
  }

  @Test
  void shouldMigrateRepositoryConfig() {
    Map<String, String> mockedValues =
      ImmutableMap.of(
        "webhooks", "http://example.com/${repositoryName};true;true;POST|"
      );
    testUtil.mockRepositoryProperties(new V1PropertyDaoTestUtil.PropertiesForRepository(REPO_NAME, mockedValues));
    when(keyGenerator.createKey()).thenReturn("42");

    updateStep.doUpdate();

    SimpleWebHook v2Webhook = new SimpleWebHook("http://example.com/${repositoryName}", true, true, HttpMethod.POST, emptyList());

    assertThat(configStore.get().getWebhooks()).extracting("configuration").contains(v2Webhook);
    assertThat(configStore.get().getWebhooks()).extracting("id").contains("42");
    assertThat(configStore.get().getWebhooks()).extracting("name").contains("SimpleWebHook");
  }

  @Test
  void shouldMigrateRepositoryConfigWithMultipleWebhooks() {
    Map<String, String> mockedValues =
      ImmutableMap.of(
        "webhooks", "http://example.com/${repositoryName};true;true;POST|http://example.com/${zweiterWebhook};undefined;true;AUTO|http://example.com/${dritteWebhook};false;false;PUT|"
      );
    testUtil.mockRepositoryProperties(new V1PropertyDaoTestUtil.PropertiesForRepository(REPO_NAME, mockedValues));

    updateStep.doUpdate();

    SimpleWebHook v2Webhook1 = new SimpleWebHook("http://example.com/${repositoryName}", true, true, HttpMethod.POST,emptyList());
    SimpleWebHook v2Webhook2 = new SimpleWebHook("http://example.com/${zweiterWebhook}", false, true, HttpMethod.AUTO, emptyList());
    SimpleWebHook v2Webhook3 = new SimpleWebHook("http://example.com/${dritteWebhook}", false, false, HttpMethod.PUT, emptyList());

    assertThat(configStore.get().getWebhooks())
      .extracting("configuration")
      .contains(v2Webhook1, v2Webhook2, v2Webhook3)
      .hasSize(3);
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

    SimpleWebHook v2Webhook = new SimpleWebHook("http://example.com/${repositoryName}", true, true, HttpMethod.AUTO,emptyList());

    assertThat(configStore.get().getWebhooks()).extracting("configuration").contains(v2Webhook);
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

