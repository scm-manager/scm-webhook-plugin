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

import com.github.legman.Subscribe;
import com.google.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sonia.scm.EagerSingleton;
import sonia.scm.plugin.Extension;
import sonia.scm.repository.PostReceiveRepositoryHookEvent;
import sonia.scm.repository.Repository;

import java.util.Set;

@Extension
@EagerSingleton
public class RepositoryWebHook {

  private static final Logger logger = LoggerFactory.getLogger(RepositoryWebHook.class);

  private final WebHookContext context;

  private final Set<DtoAdapterWebHookSpecification> specifications;

  @Inject
  public RepositoryWebHook(WebHookContext context, Set<DtoAdapterWebHookSpecification> specifications) {
    this.context = context;
    this.specifications = specifications;
  }

  @Subscribe
  public void handleEvent(PostReceiveRepositoryHookEvent event) {
    Repository repository = event.getRepository();

    if (repository != null) {
      WebHookConfiguration configuration = context.getAllConfigurations(repository);
      if (configuration.isWebHookAvailable()) {

        executeWebHooks(configuration, repository, event);
      } else if (logger.isDebugEnabled()) {
        logger.debug("no webhook defined for repository {}",
          repository.getName());
      }
    } else if (logger.isErrorEnabled()) {
      logger.error("received hook without repository");
    }
  }

  private void executeWebHooks(WebHookConfiguration configuration,
                               Repository repository,
                               PostReceiveRepositoryHookEvent event) {
    if (logger.isDebugEnabled()) {
      logger.debug("execute webhooks for repository {}", repository.getName());
    }

    for (WebHook webHook : configuration.getWebhooks()) {
      try {
        runWebhook(repository, event, webHook);
      } catch (Exception e) {
        logger.error("error while running webhook of type {} in repository {}", webHook.name, repository, e);
      }
    }
  }

  @SuppressWarnings({"unchecked"})
  private void runWebhook(Repository repository, PostReceiveRepositoryHookEvent event, WebHook webHook) {
    if (webHook.getConfiguration() == null) {
      logger.warn("skipping webhook with unknown configuration type {} for repository {}", webHook.getName(), repository);
      return;
    }
    specifications
      .stream()
      .filter(provider -> provider.handles(webHook.getConfiguration().getClass()))
      .findFirst()
      .filter(specification -> specification.supportsRepository(repository))
      .orElseGet(NoSpecificationFound::new)
      .createExecutor(webHook.getConfiguration(), repository, event)
      .run();
  }

  private static class NoSpecificationFound implements WebHookSpecification<SingleWebHookConfiguration> {
    @Override
    public Class<SingleWebHookConfiguration> getSpecificationType() {
      return null;
    }

    @Override
    public WebHookExecutor createExecutor(SingleWebHookConfiguration webHook, Repository repository, PostReceiveRepositoryHookEvent iterable) {
      return () -> logger.warn("no executor found for webhook of type {} in hook for repository {}", webHook.getClass(), repository);
    }
  }
}
