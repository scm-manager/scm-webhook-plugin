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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sonia.scm.migration.UpdateStep;
import sonia.scm.plugin.Extension;
import sonia.scm.security.KeyGenerator;
import sonia.scm.store.ConfigurationStore;
import sonia.scm.store.ConfigurationStoreFactory;
import sonia.scm.update.V1Properties;
import sonia.scm.update.V1PropertyDAO;
import sonia.scm.version.Version;
import sonia.scm.webhook.SimpleWebHook;
import sonia.scm.webhook.HttpMethod;
import sonia.scm.webhook.WebHook;
import sonia.scm.webhook.WebHookConfiguration;

import jakarta.inject.Inject;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Optional;
import java.util.Set;

import static java.util.Arrays.stream;
import static java.util.Collections.emptyList;
import static java.util.Optional.empty;
import static java.util.Optional.of;
import static java.util.stream.Collectors.toSet;
import static sonia.scm.update.V1PropertyReader.REPOSITORY_PROPERTY_READER;
import static sonia.scm.version.Version.parse;

@Extension
public class WebhooksV2ConfigMigrationUpdateStep implements UpdateStep {

  private static final Logger LOG = LoggerFactory.getLogger(WebhooksV2ConfigMigrationUpdateStep.class);

  private final V1PropertyDAO v1PropertyDAO;
  private final ConfigurationStoreFactory storeFactory;
  private final KeyGenerator keyGenerator;

  @Inject
  public WebhooksV2ConfigMigrationUpdateStep(V1PropertyDAO v1PropertyDAO, ConfigurationStoreFactory storeFactory, KeyGenerator keyGenerator) {
    this.v1PropertyDAO = v1PropertyDAO;
    this.storeFactory = storeFactory;
    this.keyGenerator = keyGenerator;
  }

  @Override public void doUpdate() {
    v1PropertyDAO
      .getProperties(REPOSITORY_PROPERTY_READER)
      .havingAllOf("webhooks")
      .forEachEntry((key, properties) ->
        buildConfig(key, properties).ifPresent(configuration ->
          setRepositoryConfiguration(configuration, key))
      );
  }

  private Optional<WebHookConfiguration> buildConfig(String repositoryId, V1Properties properties) {
    LOG.debug("migrating repository specific webhook configuration for repository id {}", repositoryId);

    String v1Webhook = properties.get("webhooks");
    if (Strings.isNullOrEmpty(v1Webhook)) {
      return empty();
    }
    String[] splittedWebhooks = v1Webhook.split("\\|");

    Set<WebHook> webhooksSet = stream(splittedWebhooks)
      .map(this::createWebHook)
      .map(config -> new WebHook(config, keyGenerator.createKey()))
      .collect(toSet());

    return of(new WebHookConfiguration(webhooksSet));
  }

  private SimpleWebHook createWebHook(String webhook) {
    String[] splitProperties = webhook.split(";");

    // Set HTTP method to AUTO if missing
    if (splitProperties.length == 3) {
      ArrayList<String> properties = new ArrayList<>(Arrays.asList(splitProperties));
      properties.add("AUTO");
      splitProperties = properties.toArray(splitProperties);
    }

    return new SimpleWebHook(
      splitProperties[0],
      Boolean.parseBoolean(splitProperties[1]),
      Boolean.parseBoolean(splitProperties[2]),
      Enum.valueOf(HttpMethod.class, splitProperties[3]),
      emptyList()
    );
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
