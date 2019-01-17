/*
 * Copyright (c) 2010, Sebastian Sdorra
 * All rights reserved.
 * <p>
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * <p>
 * 1. Redistributions of source code must retain the above copyright notice,
 * this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation
 * and/or other materials provided with the distribution.
 * 3. Neither the name of SCM-Manager; nor the names of its
 * contributors may be used to endorse or promote products derived from this
 * software without specific prior written permission.
 * <p>
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE REGENTS OR CONTRIBUTORS BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 * <p>
 * http://bitbucket.org/sdorra/scm-manager
 */


package sonia.scm.webhook;

//~--- non-JDK imports --------------------------------------------------------

import com.github.legman.Subscribe;
import com.google.inject.Inject;
import com.google.inject.Provider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sonia.scm.plugin.Extension;
import sonia.scm.repository.Changeset;
import sonia.scm.repository.PostReceiveRepositoryHookEvent;
import sonia.scm.repository.Repository;
import sonia.scm.repository.api.HookContext;
import sonia.scm.repository.api.HookFeature;
import sonia.scm.webhook.impl.JexlUrlParser;

//~--- JDK imports ------------------------------------------------------------

/**
 *
 * @author Sebastian Sdorra
 */
@Extension
public class RepositoryWebHook {

  /**
   * the logger for RepositoryWebHook
   */
  private static final Logger logger =
    LoggerFactory.getLogger(RepositoryWebHook.class);

  private final WebHookContext context;

  private final Provider<WebHookHttpClient> httpClientProvider;

  private final UrlParser urlParser;

  @Inject
  public RepositoryWebHook(Provider<WebHookHttpClient> httpClientProvider,
                           WebHookContext context) {
    this.httpClientProvider = httpClientProvider;
    this.urlParser = new JexlUrlParser();
    this.context = context;
  }

  @Subscribe
  public void handleEvent(PostReceiveRepositoryHookEvent event) {
    Repository repository = event.getRepository();

    if (repository != null) {
      WebHookConfiguration configuration = context.getRepositoryConfiguration(repository);
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


  /**
   * Method description
   *
   * @param configuration
   * @param repository
   * @param changesets
   */
  private void executeWebHooks(WebHookConfiguration configuration,
                               Repository repository, Iterable<Changeset> changesets) {
    if (logger.isDebugEnabled()) {
      logger.debug("execute webhooks for repository {}", repository.getName());
    }

    for (WebHook webHook : configuration.getWebhooks()) {
      new WebHookExecutor(httpClientProvider.get(), urlParser, webHook,
        repository, changesets).run();
    }
  }

}
