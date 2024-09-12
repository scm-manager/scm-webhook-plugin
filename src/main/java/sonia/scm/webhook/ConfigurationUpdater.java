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

import org.apache.commons.lang.StringUtils;
import sonia.scm.security.KeyGenerator;

import jakarta.inject.Inject;
import java.util.Objects;
import java.util.Optional;

class ConfigurationUpdater {

  private final AvailableWebHookSpecifications specifications;
  private final KeyGenerator keyGenerator;

  @Inject
  ConfigurationUpdater(AvailableWebHookSpecifications specifications, KeyGenerator keyGenerator) {
    this.specifications = specifications;
    this.keyGenerator = keyGenerator;
  }

  WebHookConfiguration update(WebHookConfiguration oldConfiguration, WebHookConfiguration newConfiguration) {
    newConfiguration.getWebhooks()
      .forEach(webHook -> {
        if (StringUtils.isEmpty(webHook.getId())) {
          webHook.setId(keyGenerator.createKey());
        }
        Optional<? extends SingleWebHookConfiguration> oldSingleConfiguration = find(oldConfiguration, webHook.getId());
        specifications.specificationFor(webHook.getName())
          .ifPresent(spec ->
            oldSingleConfiguration.ifPresent(configuration -> spec.updateBeforeStore(configuration, webHook.getConfiguration())));
      });
    return newConfiguration;
  }

  private Optional<SingleWebHookConfiguration> find(WebHookConfiguration oldConfiguration, String id) {
    if (oldConfiguration == null || oldConfiguration.getWebhooks() == null) {
      return Optional.empty();
    }
    return oldConfiguration.getWebhooks()
      .stream()
      .filter(webHook -> id.equals(webHook.getId()))
      .map(WebHook::getConfiguration)
      .filter(Objects::nonNull)
      .findFirst();
  }
}
