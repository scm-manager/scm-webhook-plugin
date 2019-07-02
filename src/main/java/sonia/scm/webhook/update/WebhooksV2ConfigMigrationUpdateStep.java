package sonia.scm.webhook.update;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sonia.scm.migration.UpdateStep;
import sonia.scm.plugin.Extension;
import sonia.scm.update.V1Properties;
import sonia.scm.update.V1PropertyDAO;
import sonia.scm.version.Version;
import sonia.scm.webhook.HttpMethod;
import sonia.scm.webhook.WebHook;
import sonia.scm.webhook.WebHookConfiguration;
import sonia.scm.webhook.WebHookContext;

import javax.inject.Inject;

import java.util.HashSet;
import java.util.Set;

import static sonia.scm.update.V1PropertyReader.REPOSITORY_PROPERTY_READER;
import static sonia.scm.version.Version.parse;

@Extension
public class WebhooksV2ConfigMigrationUpdateStep implements UpdateStep {

  private static final Logger LOG = LoggerFactory.getLogger(WebhooksV2ConfigMigrationUpdateStep.class);

  private final V1PropertyDAO v1PropertyDAO;
  private final WebHookContext context;

  @Inject
  public WebhooksV2ConfigMigrationUpdateStep(V1PropertyDAO v1PropertyDAO, WebHookContext context) {
    this.v1PropertyDAO = v1PropertyDAO;
    this.context = context;
  }

  @Override
  public void doUpdate() {
    v1PropertyDAO
      .getProperties(REPOSITORY_PROPERTY_READER)
      .havingAllOf("webhooks")
      .forEachEntry((key, properties) -> context.setRepositoryConfiguration(buildConfig(key,properties), key));
  }

  private WebHookConfiguration buildConfig(String repositoryId, V1Properties properties) {
    LOG.debug("migrating repository specific webhook configuration for repository id {}", repositoryId);

    String[] splittedProperties = properties.get("webhooks").split(";");

    WebHook webHook = new WebHook(
      splittedProperties[0],
      Boolean.parseBoolean(splittedProperties[1]),
      Boolean.parseBoolean(splittedProperties[2]),
      Enum.valueOf(HttpMethod.class, splittedProperties[3].substring(0,splittedProperties[3].length() - 1))
    );
    Set<WebHook> webhooks = new HashSet<>();
    webhooks.add(webHook);
    return new WebHookConfiguration(webhooks);
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
