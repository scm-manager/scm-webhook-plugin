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

package sonia.scm.webhook;

import com.google.common.io.Resources;
import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sonia.scm.plugin.PluginLoader;
import sonia.scm.repository.NamespaceAndName;
import sonia.scm.repository.Repository;
import sonia.scm.repository.RepositoryManager;
import sonia.scm.repository.RepositoryTestData;
import sonia.scm.store.ConfigurationStore;
import sonia.scm.store.ConfigurationStoreFactory;

import jakarta.xml.bind.JAXB;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Note: File TestConfiguration.testClass provides the configuration information. If tests unexpectedly fail, it may be necessary to recompile it from a Java class file.
 */

@ExtendWith(MockitoExtension.class)
class WebHookContextTest {

  @Mock(answer = Answers.RETURNS_DEEP_STUBS)
  private ConfigurationStoreFactory storeFactory;
  private final InMemoryByteDataStore globalStore = new InMemoryByteDataStore();
  private final InMemoryByteDataStore repositoryStore = new InMemoryByteDataStore();
  @Mock
  private RepositoryManager repositoryManager;
  @Mock
  private ConfigurationUpdater configurationUpdater;

  private WebHookContext context;

  private final Repository repository = RepositoryTestData.createHeartOfGold();

  @BeforeEach
  @SuppressWarnings("unchecked")
  void initMocks() {
    when(storeFactory.withType(WebHookConfiguration.class).withName("webhook").build()).thenReturn(globalStore);
    when(storeFactory.withType(WebHookConfiguration.class).withName("webhook").forRepository(repository).build()).thenReturn(repositoryStore);
    PluginLoader pluginLoader = mock(PluginLoader.class);
    when(pluginLoader.getUberClassLoader()).thenReturn(new TestClassLoader());
    when(repositoryManager.get(new NamespaceAndName("hitchhiker", "HeartOfGold"))).thenReturn(repository);
    context = new WebHookContext(storeFactory, repositoryManager, pluginLoader, configurationUpdater);
  }

  @Test
  void shouldGetGlobalConfiguration() throws IOException {
    mockConfigurationInGlobalStore();
    WebHookConfiguration configurations = context.getAllConfigurations(repository);
    List<WebHook> webhooks = configurations.getWebhooks();
    assertThat(webhooks)
      .hasSize(1)
      .extracting("configuration.configuration")
      .contains("global setting");
  }

  @Test
  void shouldGetRepositoryConfiguration() throws IOException {
    mockConfigurationInRepositoryStore();

    WebHookConfiguration configurations = context.getAllConfigurations(repository);

    List<WebHook> webhooks = configurations.getWebhooks();
    assertThat(webhooks)
      .hasSize(1)
      .extracting("configuration.configuration")
      .contains("repository setting");
  }

  @Test
  void shouldGetMergedConfiguration() throws IOException {
    mockConfigurationInGlobalStore();
    mockConfigurationInRepositoryStore();

    WebHookConfiguration configurations = context.getAllConfigurations(repository);

    List<WebHook> webhooks = configurations.getWebhooks();
    assertThat(webhooks)
      .hasSize(2)
      .extracting("configuration.configuration")
      .contains("global setting", "repository setting");
  }

  private void mockConfigurationInGlobalStore() throws IOException {
    globalStore.setConfiguration(getXmlStore("sonia/scm/webhook/globalWebhookStore.xml"));
  }

  private void mockConfigurationInRepositoryStore() throws IOException {
    repositoryStore.setConfiguration(getXmlStore("sonia/scm/webhook/localWebhookStore.xml"));
  }

  @SuppressWarnings("UnstableApiUsage")
  private static byte[] getXmlStore(String resourceName) throws IOException {
    return Resources.toByteArray(Resources.getResource(resourceName));
  }

  private static class TestClassLoader extends ClassLoader {
    @Override
    public Class<?> loadClass(String name) throws ClassNotFoundException {
      return super.loadClass(name);
    }

    @Override
    protected Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
      return super.loadClass(name, resolve);
    }

    @SneakyThrows
    @Override
    public Class<?> findClass(String name) {
      if ("sonia.scm.webhook.TestConfiguration".equals(name)) {
        byte[] bytes = getXmlStore("sonia/scm/webhook/TestConfiguration.testClass");
        return defineClass(name, bytes, 0, bytes.length);
      }
      throw new ClassNotFoundException("could not find class " + name);
    }
  }

  @SuppressWarnings("rawtypes")
  private static class InMemoryByteDataStore implements ConfigurationStore {
    private byte[] store;

    void setConfiguration(byte[] xml) {
      store = xml;
    }

    @Override
    public void set(Object item) {
      ByteArrayOutputStream baos = new ByteArrayOutputStream();
      JAXB.marshal(item, baos);
      store = baos.toByteArray();
    }

    @Override
    public WebHookConfiguration get() {
      if (store != null) {
        return JAXB.unmarshal(new ByteArrayInputStream(store), WebHookConfiguration.class);
      }
      return null;
    }
  }
}
