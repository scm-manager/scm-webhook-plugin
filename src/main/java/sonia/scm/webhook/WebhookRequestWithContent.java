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

import sonia.scm.net.ahc.AdvancedHttpRequestWithBody;

class WebhookRequestWithContent extends WebhookRequest {
  private final AdvancedHttpRequestWithBody request;

  WebhookRequestWithContent(AdvancedHttpRequestWithBody request) {
    super(request);
    this.request = request;
  }

  @Override
  WebhookRequest addHeader(WebHookExecutionHeader header) {
    request.header(header.getKey(), header.getValue(request.getContent()));
    return this;
  }
}
