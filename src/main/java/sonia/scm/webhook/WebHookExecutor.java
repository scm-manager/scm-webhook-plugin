/**
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

//~--- non-JDK imports --------------------------------------------------------

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sonia.scm.repository.Changeset;
import sonia.scm.repository.Repository;
import sonia.scm.webhook.data.ImmutableEncodedChangeset;
import sonia.scm.webhook.data.ImmutableEncodedRepository;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

//~--- JDK imports ------------------------------------------------------------

/**
 *
 * @author Sebastian Sdorra
 */
public class WebHookExecutor implements Runnable {

  /**
   * the logger for WebHookExecutor
   */
  private static final Logger logger =
    LoggerFactory.getLogger(WebHookExecutor.class);

  //~--- constructors ---------------------------------------------------------

  /**
   * Constructs ...
   *
   *
   *
   * @param httpClient
   * @param urlParser
   * @param webHook
   * @param repository
   * @param changesets
   */
  public WebHookExecutor(WebHookHttpClient httpClient, UrlParser urlParser,
                         WebHook webHook, Repository repository, Iterable<Changeset> changesets) {
    this.httpClient = httpClient;
    this.expression = urlParser.parse(webHook.getUrlPattern());
    this.webHook = webHook;
    this.repository = repository;
    this.changesets = changesets;
  }

  //~--- methods --------------------------------------------------------------

  /**
   * Method description
   *
   */
  @Override
  public void run() {
    logger.debug("execute webhook: {}", webHook);

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

  /**
   * Method description
   *
   *
   * @param repository
   *
   * @return
   */
  private Map<String, Object> createBaseEnvironment(Repository repository) {
    Map<String, Object> env = new HashMap<>();

    env.put("repository", new ImmutableEncodedRepository(repository));

    return env;
  }

  /**
   * Method description
   *
   *
   * @param repository
   * @param changeset
   *
   * @return
   */
  private String createUrl(Repository repository, Changeset changeset) {
    Map<String, Object> env = createBaseEnvironment(repository);

    ImmutableEncodedChangeset iec = new ImmutableEncodedChangeset(changeset);

    env.put("changeset", iec);
    env.put("commit", iec);

    return expression.evaluate(env);
  }

  /**
   * Method description
   *
   *
   * @param repository
   * @param changesets
   *
   * @return
   */
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

  /**
   * Method description
   *
   *
   * @param url
   * @param data
   */
  private void execute(HttpMethod method, String url, Object data) {
    if (logger.isInfoEnabled()) {
      logger.info("execute webhook for url {}", url);
    }

    try {
      httpClient.execute(method, url, data);
    } catch (IOException ex) {
      logger.error("error during webhook execution for ".concat(url), ex);
    }
  }

  //~--- fields ---------------------------------------------------------------

  /** Field description */
  private final Iterable<Changeset> changesets;

  /** Field description */
  private final UrlExpression expression;

  /** Field description */
  private final WebHookHttpClient httpClient;

  /** Field description */
  private final Repository repository;

  /** Field description */
  private final WebHook webHook;
}
