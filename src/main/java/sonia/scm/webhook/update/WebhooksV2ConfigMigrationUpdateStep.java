package sonia.scm.webhook.update;

import com.google.common.base.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sonia.scm.migration.UpdateStep;
import sonia.scm.plugin.Extension;
import sonia.scm.store.ConfigurationStore;
import sonia.scm.store.ConfigurationStoreFactory;
import sonia.scm.update.V1Properties;
import sonia.scm.update.V1PropertyDAO;
import sonia.scm.version.Version;
import sonia.scm.webhook.HttpMethod;
import sonia.scm.webhook.WebHook;
import sonia.scm.webhook.WebHookConfiguration;

import javax.inject.Inject;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import static java.util.Optional.empty;
import static java.util.Optional.of;
import static sonia.scm.update.V1PropertyReader.REPOSITORY_PROPERTY_READER;
import static sonia.scm.version.Version.parse;

@Extension
public class WebhooksV2ConfigMigrationUpdateStep implements UpdateStep {

  private static final Logger LOG = LoggerFactory.getLogger(WebhooksV2ConfigMigrationUpdateStep.class);

  private final V1PropertyDAO v1PropertyDAO;
  private final ConfigurationStoreFactory storeFactory;

  @Inject
  public WebhooksV2ConfigMigrationUpdateStep(V1PropertyDAO v1PropertyDAO, ConfigurationStoreFactory storeFactory) {
    this.v1PropertyDAO = v1PropertyDAO;
    this.storeFactory = storeFactory;
  }

  @Override public void doUpdate() {
    v1PropertyDAO
      .getProperties(REPOSITORY_PROPERTY_READER)
      .havingAllOf("webhooks")
      .forEachEntry((key, properties) -> {
        buildConfig(key, properties).ifPresent(configuration ->
          setRepositoryConfiguration(configuration, key));
      }
      );
  }

  private Optional<WebHookConfiguration> buildConfig(String repositoryId, V1Properties properties) {
    LOG.debug("migrating repository specific webhook configuration for repository id {}", repositoryId);

    String v1Webhook = properties.get("webhooks");
    if (Strings.isNullOrEmpty(v1Webhook)) {
      return empty();
    }
    String[] splittedWebhooks = v1Webhook.split("\\|");
    Set<WebHook> webhooksSet = new HashSet<>();

    for (String webhook : splittedWebhooks) {
      String[] splittedProperties = webhook.split(";");

      WebHook v2WebHook = new WebHook(
        splittedProperties[0],
        Boolean.parseBoolean(splittedProperties[1]),
        Boolean.parseBoolean(splittedProperties[2]),
        Enum.valueOf(HttpMethod.class, splittedProperties[3])
      );

      webhooksSet.add(v2WebHook);
    }
    return of(new WebHookConfiguration(webhooksSet));
  }

  void setRepositoryConfiguration(WebHookConfiguration configuration, String repositoryId) {
    ConfigurationStore<WebHookConfiguration> repositoryStore = getRepositoryStore(repositoryId);
    repositoryStore.set(configuration);
  }

  private ConfigurationStore<WebHookConfiguration> getRepositoryStore(String repositoryId) {
    return storeFactory
      .withType(WebHookConfiguration.class)
      .withName("webhook")
      .forRepository(repositoryId)
      .build();
  }

  @Override
  public Version getTargetVersion() {
    return parse("2.0.0");
  }

  @Override
  public String getAffectedDataType() {
    return "sonia.scm.webhook.config.repository.xml";
  }
}
