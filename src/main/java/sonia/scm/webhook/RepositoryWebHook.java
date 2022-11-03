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
package sonia.scm.webhook;

import com.cloudogu.scm.el.ElParser;
import com.github.legman.Subscribe;
import com.google.inject.Inject;
import com.google.inject.Provider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sonia.scm.EagerSingleton;
import sonia.scm.plugin.Extension;
import sonia.scm.repository.Changeset;
import sonia.scm.repository.PostReceiveRepositoryHookEvent;
import sonia.scm.repository.Repository;
import sonia.scm.repository.api.HookContext;
import sonia.scm.repository.api.HookFeature;

import java.util.Set;

@Extension
@EagerSingleton
public class RepositoryWebHook {

  private static final Logger logger = LoggerFactory.getLogger(RepositoryWebHook.class);

  private final WebHookContext context;

  private final Set<WebHookExecutorProvider> executorProviders;

  @Inject
  public RepositoryWebHook(Provider<WebHookHttpClient> httpClientProvider,
                           WebHookContext context, ElParser elParser, Set<WebHookExecutorProvider> executorProviders) {
    this.context = context;
    this.executorProviders = executorProviders;
  }

  @Subscribe
  public void handleEvent(PostReceiveRepositoryHookEvent event) {
    Repository repository = event.getRepository();

    if (repository != null) {
      WebHookConfiguration configuration = context.getAllConfigurations(repository);
      if (configuration.isWebHookAvailable()) {

        Iterable<Changeset> changesets = getChangesets(event, repository);
        executeWebHooks(configuration, repository, changesets);
      } else if (logger.isDebugEnabled()) {
        logger.debug("no webhook defined for repository {}",
          repository.getName());
      }
    } else if (logger.isErrorEnabled()) {
      logger.error("received hook without repository");
    }
  }

  private Iterable<Changeset> getChangesets(PostReceiveRepositoryHookEvent event, Repository repository) {
    Iterable<Changeset> changesets = null;

    if (event.getContext() != null) {
      HookContext eventContext = event.getContext();

      if (eventContext.isFeatureSupported(HookFeature.CHANGESET_PROVIDER)) {
        changesets = eventContext.getChangesetProvider()
          .setDisablePreProcessors(true)
          .getChangesets();
      } else {
        logger.debug("{} does not support changeset provider",
          repository.getType());
      }
    } else {
      logger.debug("{} has no hook context support", repository.getType());
    }

    return changesets;
  }

  private void executeWebHooks(WebHookConfiguration configuration,
                               Repository repository, Iterable<Changeset> changesets) {
    if (logger.isDebugEnabled()) {
      logger.debug("execute webhooks for repository {}", repository.getName());
    }

    for (WebHook webHook : configuration.getWebhooks()) {
      executorProviders
        .stream()
        .filter(provider -> provider.handles(webHook.getClass()))
        .findFirst()
        .ifPresent(webHookExecutorProvider -> webHookExecutorProvider.createExecutor(webHook, repository, changesets).run());
    }
  }
}
