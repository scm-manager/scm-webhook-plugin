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
import sonia.scm.xml.EncryptionUtil;

import java.io.IOException;
import java.util.List;

class WebhookRequest<T extends BaseHttpRequest<T>> {

  private static final String SPAN_KIND = "Webhook";
  private static final Logger LOG = LoggerFactory.getLogger(WebhookRequest.class);

  private final T request;

  WebhookRequest(T request) {
    this.request = request;
  }

  WebhookRequest<T> headers(List<WebhookHeader> headers) {
    for (WebhookHeader header : headers) {
      request.header(header.getKey(), EncryptionUtil.decrypt(header.getValue()));
    }
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
