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
import sonia.scm.repository.NamespaceAndName;
import sonia.scm.repository.Repository;
import sonia.scm.repository.RepositoryManager;
import sonia.scm.repository.RepositoryPermissions;
import sonia.scm.store.ConfigurationStore;
import sonia.scm.store.ConfigurationStoreFactory;

import java.util.Optional;

/**
 * @author Sebastian Sdorra
 */
@Singleton
public class WebHookContext {

  public static final String WEB_HOOK_ID = "webhook";
  private final ConfigurationStore<WebHookConfiguration> store;
  private final ConfigurationStoreFactory storeFactory;
  private WebHookConfiguration globalConfiguration;
  private static final String STORE_NAME = "webhook";
  private final RepositoryManager repositoryManager;

  @Inject
  public WebHookContext(ConfigurationStoreFactory storeFactory, RepositoryManager repositoryManager) {
    this.storeFactory = storeFactory;
    this.store = storeFactory.withType(WebHookConfiguration.class).withName(STORE_NAME).build();
    this.repositoryManager = repositoryManager;
    globalConfiguration = store.get();
    if (globalConfiguration == null) {
      globalConfiguration = new WebHookConfiguration();
    }
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
    return globalConfiguration;
  }

  public void setGlobalConfiguration(WebHookConfiguration globalConfiguration) {
    this.globalConfiguration = globalConfiguration;
    store.set(globalConfiguration);
  }

  public WebHookConfiguration getRepositoryConfigurations(String namespace, String name) {
    ConfigurationStore<WebHookConfiguration> repositoryStore = getRepositoryStore(namespace, name);
    return Optional.ofNullable(repositoryStore.get()).orElse(new WebHookConfiguration());
  }

  public WebHookConfiguration getAllConfigurations(Repository repository) {
    ConfigurationStore<WebHookConfiguration> repositoryStore = getRepositoryStore(repository);
    WebHookConfiguration repositoryConfiguration = repositoryStore.getOptional().orElse(new WebHookConfiguration());
    return getGlobalConfiguration().merge(repositoryConfiguration);
  }

  public void setRepositoryConfiguration(WebHookConfiguration configuration, String namespace, String name) {
    ConfigurationStore<WebHookConfiguration> repositoryStore = getRepositoryStore(namespace, name);
    repositoryStore.set(configuration);
  }

  private ConfigurationStore<WebHookConfiguration> getRepositoryStore(String namespace, String name) {
    Repository repository = repositoryManager.get(new NamespaceAndName(namespace, name));
    return getRepositoryStore(repository);
  }

  private ConfigurationStore<WebHookConfiguration> getRepositoryStore(Repository repository) {
    return storeFactory
      .withType(WebHookConfiguration.class)
      .withName(STORE_NAME)
      .forRepository(repository)
      .build();
  }
}
