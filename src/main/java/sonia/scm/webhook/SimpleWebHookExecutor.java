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
import com.cloudogu.scm.el.Expression;
import com.cloudogu.scm.el.env.ImmutableEncodedChangeset;
import com.cloudogu.scm.el.env.ImmutableEncodedRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sonia.scm.net.ahc.BaseHttpRequest;
import sonia.scm.repository.Changeset;
import sonia.scm.repository.Repository;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

class SimpleWebHookExecutor implements WebHookExecutor {

  private static final Logger LOG = LoggerFactory.getLogger(SimpleWebHookExecutor.class);

  private final WebhookHttpClient httpClient;
  private final Expression expression;
  private final SimpleWebHook webHook;
  private final Repository repository;
  private final Iterable<Changeset> changesets;

  SimpleWebHookExecutor(WebhookHttpClient httpClient,
                        ElParser elParser,
                        SimpleWebHook webHook,
                        Repository repository,
                        Iterable<Changeset> changesets) {
    this.httpClient = httpClient;
    this.expression = elParser.parse(webHook.getUrlPattern());
    this.webHook = webHook;
    this.repository = repository;
    this.changesets = changesets;
  }

  @Override
  public void run() {
    LOG.debug("execute webhook: {}", webHook);

    if (webHook.isExecuteOnEveryCommit()) {
      handleEachCommit();
    } else {
      handleAllCommitsAtOnce();
    }
  }

  private void handleAllCommitsAtOnce() {
    String url = createUrl(repository, changesets);

    if (webHook.isSendCommitData()) {
      execute(webHook, url, new Changesets(changesets));
    } else {
      execute(webHook, url, null);
    }
  }

  private void handleEachCommit() {
    for (Changeset changeset : changesets) {
      String url = createUrl(repository, changeset);

      if (webHook.isSendCommitData()) {
        execute(webHook, url, changeset);
      } else {
        execute(webHook, url, null);
      }
    }
  }

  private String createUrl(Repository repository, Changeset changeset) {
    Map<String, Object> env = createBaseEnvironment(repository);

    ImmutableEncodedChangeset iec = new ImmutableEncodedChangeset(changeset);

    env.put("changeset", iec);
    env.put("commit", iec);

    return expression.evaluate(env);
  }

  private String createUrl(Repository repository,
                           Iterable<Changeset> changesets) {
    Map<String, Object> env = createBaseEnvironment(repository);
    Iterator<Changeset> it = changesets.iterator();
    Changeset changeset = it.next();

    env.put("last", new ImmutableEncodedChangeset(changeset));

    while (it.hasNext()) {
      changeset = it.next();
    }

    env.put("first", new ImmutableEncodedChangeset(changeset));

    return expression.evaluate(env);
  }

  private Map<String, Object> createBaseEnvironment(Repository repository) {
    Map<String, Object> env = new HashMap<>();

    env.put("repository", new ImmutableEncodedRepository(repository));

    return env;
  }

  private void execute(SimpleWebHook webHook, String url, Object data) {
    if (LOG.isInfoEnabled()) {
      LOG.info("execute webhook for url {}", url);
    }

    try {
      createWebhookRequest(webHook.getMethod(), url, data)
      .headers(webHook.getHeaders())
        .execute();
    } catch (Exception ex) {
      LOG.error("error during webhook execution for ".concat(url), ex);
    }
  }

  private WebhookRequest<? extends BaseHttpRequest<? extends BaseHttpRequest<?>>> createWebhookRequest(HttpMethod method, String url, Object data) {
    switch (method) {
      case AUTO:
        return httpClient.auto(url, data);
      case POST:
        return httpClient.post(url, data);
      case PUT:
        return httpClient.put(url, data);
      case GET:
        return httpClient.get(url);
      default:
        throw new IllegalArgumentException("Invalid webhook method:" + method.name());
    }
  }
}
