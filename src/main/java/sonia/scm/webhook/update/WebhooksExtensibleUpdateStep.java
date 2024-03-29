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

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
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
