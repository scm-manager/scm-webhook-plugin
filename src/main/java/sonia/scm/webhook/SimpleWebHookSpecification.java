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
import com.google.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import sonia.scm.plugin.Extension;
import sonia.scm.repository.Changeset;
import sonia.scm.repository.PostReceiveRepositoryHookEvent;
import sonia.scm.repository.Repository;
import sonia.scm.repository.api.HookContext;
import sonia.scm.repository.api.HookFeature;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@Extension
@Slf4j
public class SimpleWebHookSpecification implements WebHookSpecification<SimpleWebHook> {

  public static final String DUMMY_SECRET = "__DUMMY__";
  private final WebhookHttpClient client;
  private final ElParser elParser;

  @Inject
  public SimpleWebHookSpecification(WebhookHttpClient client, ElParser elParser) {
    this.client = client;
    this.elParser = elParser;
  }

  @Override
  public Class<SimpleWebHook> getSpecificationType() {
    return SimpleWebHook.class;
  }

  @Override
  public WebHookExecutor createExecutor(SimpleWebHook webHook, Repository repository, PostReceiveRepositoryHookEvent event) {
    Iterable<Changeset> changesets = getChangesets(event, repository);
    return new SimpleWebHookExecutor(client, elParser, webHook, repository, changesets);
  }

  @Override
  public SimpleWebHook mapToDto(SimpleWebHook configuration) {
    List<WebhookHeader> mappedHeaders = new ArrayList<>();
    for (WebhookHeader header : configuration.getHeaders()) {
      if (header.isConcealed()) {
        mappedHeaders.add(new WebhookHeader(header.getKey(), DUMMY_SECRET, header.isConcealed()));
      } else {
        mappedHeaders.add(header);
      }
    }
    configuration.setHeaders(mappedHeaders);
    return configuration;
  }

  @Override
  public void updateBeforeStore(SimpleWebHook oldConfiguration, SimpleWebHook newConfiguration) {
    List<WebhookHeader> mappedHeaders = new ArrayList<>();
    HashMap<String, WebhookHeader> oldHeaderMap = new HashMap<>();
    for (WebhookHeader header : oldConfiguration.getHeaders()) {
      oldHeaderMap.put(header.getKey(), header);
    }
    for (WebhookHeader header : newConfiguration.getHeaders()) {
      if (header.getValue().equals(DUMMY_SECRET)) {
        mappedHeaders.add(new WebhookHeader(header.getKey(), oldHeaderMap.get(header.getKey()).getValue(), header.isConcealed()));
      } else {
        mappedHeaders.add(header);
      }
    }
    newConfiguration.setHeaders(mappedHeaders);
  }

  @Override
  public Class<SimpleWebHook> getDtoType() {
    return WebHookSpecification.super.getDtoType();
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
        log.debug("{} does not support changeset provider",
          repository.getType());
      }
    } else {
      log.debug("{} has no hook context support", repository.getType());
    }

    return changesets;
  }
}
