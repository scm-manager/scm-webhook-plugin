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

import org.assertj.core.api.AbstractListAssert;
import org.assertj.core.api.ObjectAssert;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
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

import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class WebhooksExtensibleUpdateStepTest {

  @Spy
  private ConfigurationStoreFactory storeFactory = new InMemoryConfigurationStoreFactory();
  @Mock
  private RepositoryUpdateIterator repositoryUpdateIterator;

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
      singletonList(new WebHook(new SimpleWebHook("https://hog.org/", false, false, HttpMethod.PUT)))
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
      .extracting("configuration")
      .containsExactly(new SimpleWebHook("https://hog.org/", false, false, HttpMethod.PUT));
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
      .extracting("configuration")
      .containsExactly(new SimpleWebHook("https://hog.org/", false, false, HttpMethod.PUT));
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
