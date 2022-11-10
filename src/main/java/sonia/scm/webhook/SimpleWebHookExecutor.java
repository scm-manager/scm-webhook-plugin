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
import sonia.scm.repository.Changeset;
import sonia.scm.repository.Repository;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

class SimpleWebHookExecutor implements WebHookExecutor {

  private static final Logger LOG = LoggerFactory.getLogger(SimpleWebHookExecutor.class);

  private final Iterable<Changeset> changesets;
  private final Expression expression;
  private final WebHookHttpClient httpClient;
  private final Repository repository;
  private final SimpleWebHook webHook;

  SimpleWebHookExecutor(WebHookHttpClient httpClient, ElParser elParser,
                               SimpleWebHook webHook, Repository repository, Iterable<Changeset> changesets) {
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
      for (Changeset c : changesets) {
        String url = createUrl(repository, c);

        if (webHook.isSendCommitData()) {
          execute(webHook.getMethod(), url, c);
        } else {
          execute(webHook.getMethod(), url, null);
        }
      }
    } else {
      String url = createUrl(repository, changesets);

      if (webHook.isSendCommitData()) {
        execute(webHook.getMethod(), url, new Changesets(changesets));
      } else {
        execute(webHook.getMethod(), url, null);
      }
    }
  }

  private Map<String, Object> createBaseEnvironment(Repository repository) {
    Map<String, Object> env = new HashMap<>();

    env.put("repository", new ImmutableEncodedRepository(repository));

    return env;
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

  private void execute(HttpMethod method, String url, Object data) {
    if (LOG.isInfoEnabled()) {
      LOG.info("execute webhook for url {}", url);
    }

    try {
      httpClient.execute(method, url, data);
    } catch (IOException ex) {
      LOG.error("error during webhook execution for ".concat(url), ex);
    }
  }
}
