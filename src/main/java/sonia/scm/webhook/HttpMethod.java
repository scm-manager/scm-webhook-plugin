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

public enum HttpMethod {
  AUTO {
    @Override
    WebhookRequest create(WebhookHttpClient httpClient, String url, Object data) {
      return httpClient.auto(url, data);
    }
  },
  GET {
    @Override
    WebhookRequest create(WebhookHttpClient httpClient, String url, Object data) {
      return httpClient.get(url);
    }
  },
  POST {
    @Override
    WebhookRequest create(WebhookHttpClient httpClient, String url, Object data) {
      return httpClient.post(url, data);
    }
  },
  PUT {
    @Override
    WebhookRequest create(WebhookHttpClient httpClient, String url, Object data) {
      return httpClient.put(url, data);
    }
  };

  abstract WebhookRequest create(WebhookHttpClient httpClient, String url, Object data);
}
