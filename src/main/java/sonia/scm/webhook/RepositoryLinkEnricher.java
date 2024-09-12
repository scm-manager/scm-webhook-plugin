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

import de.otto.edison.hal.HalRepresentation;
import lombok.AllArgsConstructor;
import lombok.Getter;
import sonia.scm.api.v2.resources.Enrich;
import sonia.scm.api.v2.resources.HalAppender;
import sonia.scm.api.v2.resources.HalEnricher;
import sonia.scm.api.v2.resources.HalEnricherContext;
import sonia.scm.api.v2.resources.ScmPathInfoStore;
import sonia.scm.plugin.Extension;
import sonia.scm.repository.Repository;
import sonia.scm.webhook.internal.WebHookConfigurationResourceLinks;

import jakarta.inject.Inject;
import jakarta.inject.Provider;
import java.util.Collection;

@Extension
@Enrich(Repository.class)
public class RepositoryLinkEnricher implements HalEnricher {

  private final Provider<ScmPathInfoStore> scmPathInfoStoreProvider;
  private final AvailableWebHookSpecifications availableSpecifications;

  @Inject
  public RepositoryLinkEnricher(Provider<ScmPathInfoStore> scmPathInfoStoreProvider, AvailableWebHookSpecifications availableSpecifications) {
    this.scmPathInfoStoreProvider = scmPathInfoStoreProvider;
    this.availableSpecifications = availableSpecifications;
  }

  @Override
  public void enrich(HalEnricherContext context, HalAppender appender) {
    Repository repository = context.oneRequireByType(Repository.class);
    if (WebHookContext.isReadPermitted(repository)) {
      WebHookConfigurationResourceLinks resourceLinks = new WebHookConfigurationResourceLinks(scmPathInfoStoreProvider.get().get());
      appender.appendLink("webHookConfig", resourceLinks.repositoryConfigurations.self(repository.getNamespace(), repository.getName()));
    }
    if (WebHookContext.isWritePermitted(repository)) {
      appender.appendEmbedded("supportedWebHookTypes", new SupportedWebhookTypesDto(availableSpecifications.getTypesFor(repository)));
    }
  }

  @AllArgsConstructor
  private static class SupportedWebhookTypesDto extends HalRepresentation {
    @Getter
    private final Collection<String> types;
  }
}
