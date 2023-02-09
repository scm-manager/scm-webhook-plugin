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
package sonia.scm.webhook;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import sonia.scm.config.ConfigurationPermissions;
import sonia.scm.plugin.PluginLoader;
import sonia.scm.repository.NamespaceAndName;
import sonia.scm.repository.Repository;
import sonia.scm.repository.RepositoryManager;
import sonia.scm.repository.RepositoryPermissions;
import sonia.scm.store.ConfigurationStore;
import sonia.scm.store.ConfigurationStoreFactory;

import java.util.Optional;
import java.util.function.Supplier;

/**
 * @author Sebastian Sdorra
 */
@Singleton
public class WebHookContext {

  private static final String WEB_HOOK_ID = "webhook";
  private static final String STORE_NAME = "webhook";

  private final ConfigurationStoreFactory storeFactory;
  private final ClassLoader uberClassLoader;
  private final RepositoryManager repositoryManager;
  private final ConfigurationUpdater configurationUpdater;

  @Inject
  public WebHookContext(ConfigurationStoreFactory storeFactory, RepositoryManager repositoryManager, PluginLoader pluginLoader, ConfigurationUpdater configurationUpdater) {
    this.storeFactory = storeFactory;
    this.repositoryManager = repositoryManager;
    this.uberClassLoader = pluginLoader.getUberClassLoader();
    this.configurationUpdater = configurationUpdater;
  }

  public static boolean isReadPermitted() {
    return ConfigurationPermissions.read(WEB_HOOK_ID).isPermitted();
  }

  public static boolean isReadPermitted(Repository repository) {
    return RepositoryPermissions.custom(WEB_HOOK_ID, repository).isPermitted();
  }

  public static boolean isWritePermitted() {
    return ConfigurationPermissions.write(WEB_HOOK_ID).isPermitted();
  }

  public static boolean isWritePermitted(Repository repository) {
    return RepositoryPermissions.custom(WEB_HOOK_ID, repository).isPermitted();
  }

  public static void checkReadPermission() {
    ConfigurationPermissions.read(WEB_HOOK_ID).check();
  }

  public static void checkWritePermission() {
    ConfigurationPermissions.write(WEB_HOOK_ID).check();
  }

  public static void checkReadPermission(Repository repository) {
    RepositoryPermissions.custom(WEB_HOOK_ID, repository).check();
  }

  public static void checkWritePermission(Repository repository) {
    RepositoryPermissions.custom(WEB_HOOK_ID, repository).check();
  }

  public WebHookConfiguration getGlobalConfiguration() {
    return getFromStore(getGlobalStore()).orElse(new WebHookConfiguration());
  }

  public void setGlobalConfiguration(WebHookConfiguration globalConfiguration) {
    ConfigurationStore<WebHookConfiguration> store = getGlobalStore();
    withUberClassLoader(() -> {
      store.set(configurationUpdater.update(store.get(), globalConfiguration));
      return null;
    });
  }

  public WebHookConfiguration getRepositoryConfigurations(String namespace, String name) {
    ConfigurationStore<WebHookConfiguration> repositoryStore = getRepositoryStore(namespace, name);
    return getFromStore(repositoryStore).orElse(new WebHookConfiguration());
  }

  public WebHookConfiguration getAllConfigurations(Repository repository) {
    WebHookConfiguration repositoryConfiguration = getRepositoryConfigurations(repository.getNamespace(), repository.getName());
    return getGlobalConfiguration().merge(repositoryConfiguration);
  }

  public void setRepositoryConfiguration(WebHookConfiguration configuration, String namespace, String name) {
    ConfigurationStore<WebHookConfiguration> repositoryStore = getRepositoryStore(namespace, name);
    withUberClassLoader(() -> {
      repositoryStore.set(configurationUpdater.update(repositoryStore.get(), configuration));
      return null;
    });
  }

  private ConfigurationStore<WebHookConfiguration> getRepositoryStore(String namespace, String name) {
    Repository repository = repositoryManager.get(new NamespaceAndName(namespace, name));
    return getRepositoryStore(repository);
  }

  private Optional<WebHookConfiguration> getFromStore(ConfigurationStore<WebHookConfiguration> store) {
    return withUberClassLoader(store::getOptional);
  }

  private ConfigurationStore<WebHookConfiguration> getGlobalStore() {
    return storeFactory.withType(WebHookConfiguration.class).withName(STORE_NAME).build();
  }

  private <T> T withUberClassLoader(Supplier<T> runnable) {
    ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();
    Thread.currentThread().setContextClassLoader(uberClassLoader);
    try {
      return runnable.get();
    } finally {
      Thread.currentThread().setContextClassLoader(contextClassLoader);
    }
  }

  private ConfigurationStore<WebHookConfiguration> getRepositoryStore(Repository repository) {
    return storeFactory
      .withType(WebHookConfiguration.class)
      .withName(STORE_NAME)
      .forRepository(repository)
      .build();
  }
}
