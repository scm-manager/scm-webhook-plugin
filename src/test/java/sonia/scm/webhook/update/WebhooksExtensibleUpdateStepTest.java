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

import org.assertj.core.api.AbstractListAssert;
import org.assertj.core.api.ObjectAssert;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import sonia.scm.security.KeyGenerator;
import sonia.scm.store.ConfigurationStore;
import sonia.scm.store.ConfigurationStoreFactory;
import sonia.scm.store.TypedStoreParameters;
import sonia.scm.update.RepositoryUpdateIterator;
import sonia.scm.webhook.HttpMethod;
import sonia.scm.webhook.SimpleWebHook;
import sonia.scm.webhook.WebHook;
import sonia.scm.webhook.WebHookConfiguration;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class WebhooksExtensibleUpdateStepTest {

  @Spy
  private ConfigurationStoreFactory storeFactory = new InMemoryConfigurationStoreFactory();
  @Mock
  private RepositoryUpdateIterator repositoryUpdateIterator;
  @Mock
  private KeyGenerator keyGenerator;

  @InjectMocks
  WebhooksExtensibleUpdateStep updateStep;

  @BeforeEach
  void mockRepositoryIterator() {
    doAnswer(invocation -> {
      invocation.getArgument(0, RepositoryUpdateIterator.Updater.class).update("42");
      return null;
    }).when(repositoryUpdateIterator).updateEachRepository(any());
  }

  @Test
  void shouldDoNothingWithoutAnyConfigAtAll() {
    updateStep.doUpdate();

    verify(storeFactory, never()).withType(WebHookConfiguration.class);
  }

  @Test
  void shouldDoNothingIfConfigIsNewAlready() {
    WebHookConfiguration newConfiguration = new WebHookConfiguration(
      singletonList(new WebHook(new SimpleWebHook("https://hog.org/", false, false, HttpMethod.PUT, emptyList()), "42"))
    );
    storeFactory
      .withType(WebHookConfiguration.class)
      .withName("webhook")
      .build()
      .set(newConfiguration);
    reset(storeFactory);

    updateStep.doUpdate();

    verify(storeFactory, never()).withType(WebHookConfiguration.class);
  }

  @Nested
  class WithConfigurations {

    @BeforeEach
    void initKeyGenerator() {
      when(keyGenerator.createKey()).thenReturn("42");
    }

    @Test
    void shouldConvertOldGlobalConfig() {
      WebhooksExtensibleUpdateStep.OldWebHookConfiguration oldConfiguration = new WebhooksExtensibleUpdateStep.OldWebHookConfiguration();
      HashSet<WebhooksExtensibleUpdateStep.OldWebHook> webhooks = new HashSet<>();
      WebhooksExtensibleUpdateStep.OldWebHook webHook = new WebhooksExtensibleUpdateStep.OldWebHook();
      webHook.urlPattern = "https://hog.org/";
      webHook.method = HttpMethod.PUT;
      webhooks.add(webHook);
      oldConfiguration.webhooks = webhooks;
      storeFactory
        .withType(WebhooksExtensibleUpdateStep.OldWebHookConfiguration.class)
        .withName("webhook")
        .build()
        .set(oldConfiguration);

      updateStep.doUpdate();

      Optional<WebHookConfiguration> newConfiguration = storeFactory
        .withType(WebHookConfiguration.class)
        .withName("webhook")
        .build()
        .getOptional();

      AbstractListAssert<?, List<?>, Object, ObjectAssert<Object>> webhooksAssertion = assertThat(newConfiguration)
        .isPresent()
        .get()
        .extracting("webhooks")
        .asList();
      webhooksAssertion
        .extracting("name")
        .containsExactly("SimpleWebHook");
      webhooksAssertion
        .extracting("id")
        .containsExactly("42");
      webhooksAssertion
        .extracting("configuration")
        .containsExactly(new SimpleWebHook("https://hog.org/", false, false, HttpMethod.PUT, emptyList()));
    }

    @Test
    void shouldConvertOldRepositoryConfig() {
      WebhooksExtensibleUpdateStep.OldWebHookConfiguration oldConfiguration = new WebhooksExtensibleUpdateStep.OldWebHookConfiguration();
      HashSet<WebhooksExtensibleUpdateStep.OldWebHook> webhooks = new HashSet<>();
      WebhooksExtensibleUpdateStep.OldWebHook webHook = new WebhooksExtensibleUpdateStep.OldWebHook();
      webHook.urlPattern = "https://hog.org/";
      webHook.method = HttpMethod.PUT;
      webhooks.add(webHook);
      oldConfiguration.webhooks = webhooks;
      storeFactory
        .withType(WebhooksExtensibleUpdateStep.OldWebHookConfiguration.class)
        .withName("webhook")
        .forRepository("42")
        .build()
        .set(oldConfiguration);

      updateStep.doUpdate();

      Optional<WebHookConfiguration> newConfiguration = storeFactory
        .withType(WebHookConfiguration.class)
        .withName("webhook")
        .forRepository("42")
        .build()
        .getOptional();

      AbstractListAssert<?, List<?>, Object, ObjectAssert<Object>> webhooksAssertion = assertThat(newConfiguration)
        .isPresent()
        .get()
        .extracting("webhooks")
        .asList();
      webhooksAssertion
        .extracting("name")
        .containsExactly("SimpleWebHook");
      webhooksAssertion
        .extracting("id")
        .containsExactly("42");
      webhooksAssertion
        .extracting("configuration")
        .containsExactly(new SimpleWebHook("https://hog.org/", false, false, HttpMethod.PUT, emptyList()));
    }
  }

  @Test
  void shouldHandleEmptyConfig() {
    WebhooksExtensibleUpdateStep.OldWebHookConfiguration oldConfiguration = new WebhooksExtensibleUpdateStep.OldWebHookConfiguration();
    storeFactory
      .withType(WebhooksExtensibleUpdateStep.OldWebHookConfiguration.class)
      .withName("webhook")
      .build()
      .set(oldConfiguration);

    assertDoesNotThrow(() -> updateStep.doUpdate());
  }

  @SuppressWarnings({"rawtypes", "unchecked"})
  private static class InMemoryConfigurationStoreFactory implements ConfigurationStoreFactory {

    private final Map<String, InMemoryConfigurationStore> stores = new HashMap<>();

    @Override
    public ConfigurationStore getStore(TypedStoreParameters storeParameters) {
      String name = storeParameters.getName();
      String id = storeParameters.getRepositoryId();
      String type = storeParameters.getType().getSimpleName();
      return get(name, id, type);
    }

    public ConfigurationStore get(String name, String id, String type) {
      return stores.computeIfAbsent(buildKey(name, id, type), x -> new InMemoryConfigurationStore());
    }

    private String buildKey(String name, String id, String type) {
      return id == null ? buildKey(name, type) : buildKey(name, type) + "-" + id;
    }

    private static String buildKey(String name, String type) {
      return name + "-" + type;
    }

    private static class InMemoryConfigurationStore<T> implements ConfigurationStore<T> {

      private T object;

      @Override
      public T get() {
        return object;
      }

      @Override
      public void set(T object) {
        this.object = object;
      }
    }
  }
}
