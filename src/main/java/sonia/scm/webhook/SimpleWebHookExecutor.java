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

import com.cloudogu.scm.el.ElParser;
import com.cloudogu.scm.el.Expression;
import com.cloudogu.scm.el.env.ImmutableEncodedChangeset;
import com.cloudogu.scm.el.env.ImmutableEncodedRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sonia.scm.repository.Changeset;
import sonia.scm.repository.Repository;
import sonia.scm.webhook.execution.WebHookExecution;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

class SimpleWebHookExecutor implements WebHookExecutor {

  private static final Logger LOG = LoggerFactory.getLogger(SimpleWebHookExecutor.class);

  private final Expression expression;
  private final SimpleWebHook webHook;
  private final Repository repository;
  private final Iterable<Changeset> changesets;
  private final WebHookSender sender;

  SimpleWebHookExecutor(WebHookSender sender,
                        ElParser elParser,
                        SimpleWebHook webHook,
                        Repository repository,
                        Iterable<Changeset> changesets) {
    this.sender = sender;
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
    if (changesets != null) {
      for (Changeset changeset : changesets) {
        String url = createUrl(repository, changeset);
        if (webHook.isSendCommitData()) {
          execute(webHook, url, changeset);
        } else {
          execute(webHook, url, null);
        }
      }
    }
  }

  private String createUrl(Repository repository, Changeset changeset) {
    Map<String, Object> env = createBaseEnvironment(repository);

    if (changeset != null) {
      ImmutableEncodedChangeset iec = new ImmutableEncodedChangeset(changeset);

      env.put("changeset", iec);
      env.put("commit", iec);
    }

    return expression.evaluate(env);
  }

  private String createUrl(Repository repository,
                           Iterable<Changeset> changesets) {
    Map<String, Object> env = createBaseEnvironment(repository);
    if (changesets != null) {
      Iterator<Changeset> it = changesets.iterator();
      Changeset changeset = it.next();

      env.put("last", new ImmutableEncodedChangeset(changeset));

      while (it.hasNext()) {
        changeset = it.next();
      }

      env.put("first", new ImmutableEncodedChangeset(changeset));
    }

    return expression.evaluate(env);
  }

  private Map<String, Object> createBaseEnvironment(Repository repository) {
    Map<String, Object> env = new HashMap<>();

    env.put("repository", new ImmutableEncodedRepository(repository));

    return env;
  }

  private void execute(SimpleWebHook webHook, String url, Object data) {
    WebHookExecution.WebHookExecutionBuilder builder =
      WebHookExecution
        .builder()
        .httpMethod(webHook.getMethod())
        .url(url)
        .headers(webHook.getHeaders().stream().map(WebHookExecutionHeader::from).toList())
        .payload(data);
    WebHookExecution execution = builder.build();
    sender.execute(execution);
  }
}
