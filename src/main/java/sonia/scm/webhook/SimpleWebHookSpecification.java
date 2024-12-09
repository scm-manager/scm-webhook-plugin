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
  private final WebHookSender sender;
  private final ElParser elParser;

  @Inject
  public SimpleWebHookSpecification(WebHookSender sender, ElParser elParser) {
    this.sender = sender;
    this.elParser = elParser;
  }

  @Override
  public Class<SimpleWebHook> getSpecificationType() {
    return SimpleWebHook.class;
  }

  @Override
  public WebHookExecutor createExecutor(SimpleWebHook webHook, Repository repository, PostReceiveRepositoryHookEvent event) {
    Iterable<Changeset> changesets = getChangesets(event, repository);
    return new SimpleWebHookExecutor(sender, elParser, webHook, repository, changesets);
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
