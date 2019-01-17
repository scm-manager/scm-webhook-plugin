/*
 * Copyright (c) 2010, Sebastian Sdorra All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice,
 * this list of conditions and the following disclaimer. 2. Redistributions in
 * binary form must reproduce the above copyright notice, this list of
 * conditions and the following disclaimer in the documentation and/or other
 * materials provided with the distribution. 3. Neither the name of SCM-Manager;
 * nor the names of its contributors may be used to endorse or promote products
 * derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE REGENTS OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * http://bitbucket.org/sdorra/scm-manager
 *
 */


package sonia.scm.webhook;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import sonia.scm.config.ConfigurationPermissions;
import sonia.scm.repository.NamespaceAndName;
import sonia.scm.repository.Repository;
import sonia.scm.repository.api.RepositoryService;
import sonia.scm.repository.api.RepositoryServiceFactory;
import sonia.scm.store.ConfigurationStore;
import sonia.scm.store.ConfigurationStoreFactory;

import java.util.Optional;

/**
 * @author Sebastian Sdorra
 */
@Singleton
public class WebHookContext {

  public static final String WEB_HOOK_ID = "webHook";
  private final ConfigurationStore<WebHookConfiguration> store;
  private ConfigurationStoreFactory storeFactory;
  private WebHookConfiguration globalConfiguration;
  private static final String STORE_NAME = "webhook";
  private final RepositoryServiceFactory serviceFactory;

  @Inject
  public WebHookContext(ConfigurationStoreFactory storeFactory, RepositoryServiceFactory serviceFactory) {
    this.storeFactory = storeFactory;
    this.store = storeFactory.withType(WebHookConfiguration.class).withName(STORE_NAME).build();
    this.serviceFactory = serviceFactory;
    globalConfiguration = store.get();
    if (globalConfiguration == null) {
      globalConfiguration = new WebHookConfiguration();
    }
  }

  public static boolean isReadPermitted() {
    return ConfigurationPermissions.read(WEB_HOOK_ID).isPermitted();
  }

  public static boolean isWritePermitted() {
    return ConfigurationPermissions.write(WEB_HOOK_ID).isPermitted();
  }

  public static void checkReadPermission() {
    ConfigurationPermissions.read(WEB_HOOK_ID).check();
  }

  public static void checkWritePermission() {
    ConfigurationPermissions.write(WEB_HOOK_ID).check();
  }

  public WebHookConfiguration getGlobalConfiguration() {
    return globalConfiguration;
  }

  public void setGlobalConfiguration(WebHookConfiguration globalConfiguration) {
    this.globalConfiguration = globalConfiguration;
    store.set(globalConfiguration);
  }

  public WebHookConfiguration getRepositoryConfiguration(String namespace, String name) {
    ConfigurationStore<WebHookConfiguration> repositoryStore = getRepositoryStore(namespace, name);
    return Optional.ofNullable(repositoryStore.get()).orElse(new WebHookConfiguration());
  }

  public WebHookConfiguration getRepositoryConfiguration(Repository repository) {
    ConfigurationStore<WebHookConfiguration> repositoryStore = getRepositoryStore(repository);
    return Optional.ofNullable(repositoryStore.get()).orElse(new WebHookConfiguration());
  }

  public void setRepositoryConfiguration(WebHookConfiguration configuration, String namespace, String name) {
    ConfigurationStore<WebHookConfiguration> repositoryStore = getRepositoryStore(namespace, name);
    repositoryStore.set(configuration);
  }

  private ConfigurationStore<WebHookConfiguration> getRepositoryStore(String namespace, String name) {
    RepositoryService repositoryService = serviceFactory.create(new NamespaceAndName(namespace, name));
    Repository repository = repositoryService.getRepository();
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
