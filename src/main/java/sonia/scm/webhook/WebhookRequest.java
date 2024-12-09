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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sonia.scm.net.ahc.BaseHttpRequest;

import java.io.IOException;
import java.util.List;
import java.util.function.Consumer;

class WebhookRequest {

  private static final String SPAN_KIND = "Webhook";
  private static final Logger LOG = LoggerFactory.getLogger(WebhookRequest.class);

  private final BaseHttpRequest<?> request;

  WebhookRequest(BaseHttpRequest<?> request) {
    this.request = request;
  }

  WebhookRequest headers(List<WebHookExecutionHeader> headers) {
    for (WebHookExecutionHeader header : headers) {
      addHeader(header);
    }
    return this;
  }

  WebhookRequest addHeader(WebHookExecutionHeader header) {
    request.header(header.getKey(), header.getValue());
    return this;
  }

  WebhookRequest accept(Consumer<BaseHttpRequest<?>> function) {
    function.accept(request);
    return this;
  }

  void execute() throws IOException {
    String url = request.getUrl();
    int statusCode = request.spanKind(SPAN_KIND).request().getStatus();

    if ((statusCode >= 200) && (statusCode < 300)) {
      LOG.info("webhook {} ended successfully with status code {}", url, statusCode);
    } else {
      LOG.warn("webhook {} failed with statusCode {}", url, statusCode);
    }
  }
}
