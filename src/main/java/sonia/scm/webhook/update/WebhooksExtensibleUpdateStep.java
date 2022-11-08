package sonia.scm.webhook.update;

import com.google.common.base.Strings;
import com.google.inject.Inject;
import sonia.scm.migration.UpdateStep;
import sonia.scm.plugin.Extension;
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

import static sonia.scm.version.Version.parse;

@Extension
public class WebhooksExtensibleUpdateStep implements UpdateStep {

  private static final String STORE_NAME = "webhook";

  private final ConfigurationStoreFactory storeFactory;
  private final RepositoryUpdateIterator repositoryUpdateIterator;

  @Inject
  public WebhooksExtensibleUpdateStep(ConfigurationStoreFactory storeFactory, RepositoryUpdateIterator repositoryUpdateIterator) {
    this.storeFactory = storeFactory;
    this.repositoryUpdateIterator = repositoryUpdateIterator;
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
    webhooks.filter(this::isOlfConfiguration).map(this::convert).ifPresent(newConfig -> storeSupplier.get().set(newConfig));
  }

  private boolean isOlfConfiguration(OldWebHookConfiguration oldWebHookConfiguration) {
    return oldWebHookConfiguration.webhooks.stream().anyMatch(hook -> !Strings.isNullOrEmpty(hook.urlPattern) && Strings.isNullOrEmpty(hook.name));
  }

  private WebHookConfiguration convert(OldWebHookConfiguration oldWebHookConfiguration) {
    return new WebHookConfiguration(
      oldWebHookConfiguration.webhooks.stream().map(this::convert).collect(Collectors.toList())
    );
  }

  private WebHook convert(OldWebHook webHook) {
    return new WebHook(new SimpleWebHook(webHook.urlPattern, webHook.executeOnEveryCommit, webHook.sendCommitData, webHook.method));
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
