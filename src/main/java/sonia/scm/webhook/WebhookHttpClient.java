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
import jakarta.inject.Provider;
import sonia.scm.net.ahc.AdvancedHttpClient;

class WebhookHttpClient {
  private final Provider<AdvancedHttpClient> clientProvider;

  @Inject
  WebhookHttpClient(Provider<AdvancedHttpClient> clientProvider) {
    this.clientProvider = clientProvider;
  }

  WebhookRequest post(String url, Object data) {
    if (data != null) {
      return new WebhookRequestWithContent(clientProvider.get().post(url).jsonContent(data));
    }
    return new WebhookRequest(clientProvider.get().post(url));
  }

  WebhookRequest put(String url, Object data) {
    if (data != null) {
      return new WebhookRequestWithContent(clientProvider.get().put(url).jsonContent(data));
    }
    return new WebhookRequest(clientProvider.get().put(url));
  }

  WebhookRequest get(String url) {
    return new WebhookRequest(clientProvider.get().get(url));
  }

  WebhookRequest auto(String url, Object data) {
    if (data != null) {
      return post(url, data);
    }
    return get(url);
  }
}
