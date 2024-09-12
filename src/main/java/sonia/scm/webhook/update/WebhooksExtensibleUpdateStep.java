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

import com.google.common.base.Strings;
import com.google.inject.Inject;
import sonia.scm.migration.UpdateStep;
import sonia.scm.plugin.Extension;
import sonia.scm.security.KeyGenerator;
import sonia.scm.store.ConfigurationStore;
import sonia.scm.store.ConfigurationStoreFactory;
import sonia.scm.update.RepositoryUpdateIterator;
import sonia.scm.version.Version;
import sonia.scm.webhook.HttpMethod;
import sonia.scm.webhook.SimpleWebHook;
import sonia.scm.webhook.WebHook;
import sonia.scm.webhook.WebHookConfiguration;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import java.util.Optional;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import static java.util.Collections.emptyList;
import static sonia.scm.version.Version.parse;

@Extension
public class WebhooksExtensibleUpdateStep implements UpdateStep {

  private static final String STORE_NAME = "webhook";

  private final ConfigurationStoreFactory storeFactory;
  private final RepositoryUpdateIterator repositoryUpdateIterator;
  private final KeyGenerator keyGenerator;

  @Inject
  public WebhooksExtensibleUpdateStep(ConfigurationStoreFactory storeFactory, RepositoryUpdateIterator repositoryUpdateIterator, KeyGenerator keyGenerator) {
    this.storeFactory = storeFactory;
    this.repositoryUpdateIterator = repositoryUpdateIterator;
    this.keyGenerator = keyGenerator;
  }

  @Override
  public void doUpdate() {
    doGlobalUpdate();
    repositoryUpdateIterator.updateEachRepository(this::doRepositoryUpdate);
  }

  private void doGlobalUpdate() {
    Supplier<ConfigurationStore<WebHookConfiguration>> storeSupplier = () -> storeFactory.withType(WebHookConfiguration.class).withName(STORE_NAME).build();
    ConfigurationStore<OldWebHookConfiguration> oldStore = storeFactory.withType(OldWebHookConfiguration.class).withName(STORE_NAME).build();
    doUpdate(storeSupplier, oldStore);
  }

  private void doRepositoryUpdate(String repositoryId) {
    Supplier<ConfigurationStore<WebHookConfiguration>> storeSupplier = () -> storeFactory.withType(WebHookConfiguration.class).withName(STORE_NAME).forRepository(repositoryId).build();
    ConfigurationStore<OldWebHookConfiguration> oldStore = storeFactory.withType(OldWebHookConfiguration.class).withName(STORE_NAME).forRepository(repositoryId).build();
    doUpdate(storeSupplier, oldStore);
  }

  private void doUpdate(Supplier<ConfigurationStore<WebHookConfiguration>> storeSupplier, ConfigurationStore<OldWebHookConfiguration> oldStore) {
    Optional<OldWebHookConfiguration> webhooks = oldStore.getOptional();
    webhooks.filter(this::isOldConfiguration).map(this::convert).ifPresent(newConfig -> storeSupplier.get().set(newConfig));
  }

  private boolean isOldConfiguration(OldWebHookConfiguration oldWebHookConfiguration) {
    return oldWebHookConfiguration.webhooks != null &&
      oldWebHookConfiguration.webhooks.stream().anyMatch(hook -> !Strings.isNullOrEmpty(hook.urlPattern) && Strings.isNullOrEmpty(hook.name));
  }

  private WebHookConfiguration convert(OldWebHookConfiguration oldWebHookConfiguration) {
    return new WebHookConfiguration(
      oldWebHookConfiguration.webhooks.stream().map(this::convert).collect(Collectors.toList())
    );
  }

  private WebHook convert(OldWebHook webHook) {
    return new WebHook(new SimpleWebHook(webHook.urlPattern, webHook.executeOnEveryCommit, webHook.sendCommitData, webHook.method, emptyList()), keyGenerator.createKey());
  }

  @Override
  public Version getTargetVersion() {
    return parse("2.1.0");
  }

  @Override
  public String getAffectedDataType() {
    return "sonia.scm.webhook.config.repository.xml";
  }

  @XmlRootElement(name = "webhooks")
  @XmlAccessorType(XmlAccessType.FIELD)
  static class OldWebHookConfiguration {
    @XmlElement(name = "webhook")
    Set<OldWebHook> webhooks;
  }

  @XmlRootElement(name = "webhook")
  @XmlAccessorType(XmlAccessType.FIELD)
  static class OldWebHook {
    String name;
    String urlPattern;
    boolean executeOnEveryCommit;
    boolean sendCommitData;
    HttpMethod method = HttpMethod.AUTO;
  }
}
