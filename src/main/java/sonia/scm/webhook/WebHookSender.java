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

import jakarta.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sonia.scm.webhook.execution.WebHookExecution;

import java.io.IOException;

/**
 * This class provides a general-purpose means for other plugins to run their own WebHookExecutions.
 */
public class WebHookSender {

  private static final Logger LOG = LoggerFactory.getLogger(WebHookSender.class);

  private final WebhookHttpClient httpClient;

  @Inject
  WebHookSender(WebhookHttpClient httpClient) {
    this.httpClient = httpClient;
  }

  void execute(WebHookExecution webHook) {
    LOG.info("execute webhook for url {}", webHook.getUrl());

    try {
      webHook
        .getHttpMethod()
        .create(httpClient, webHook.getUrl(), webHook.getPayload())
        .headers(webHook.getHeaders())
        .accept(webHook.getPrepare())
        .execute();
    } catch (IOException ex) {
      LOG.error("error during webhook execution for {}", webHook.getUrl(), ex);
    }
  }
}
