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

  private final Set<WebHookSpecification> specifications;

  @Inject
  public RepositoryWebHook(WebHookContext context, Set<WebHookSpecification> specifications) {
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
        logger.error("error while running webhook of type {} in repository {}", webHook.configuration.getClass(), repository, e);
      }
    }
  }

  @SuppressWarnings({"unchecked"})
  private void runWebhook(Repository repository, PostReceiveRepositoryHookEvent event, WebHook webHook) {
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
