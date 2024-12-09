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

package sonia.scm.webhook.execution;

import jakarta.ws.rs.core.MediaType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import sonia.scm.net.ahc.BaseHttpRequest;
import sonia.scm.webhook.HttpMethod;
import sonia.scm.webhook.WebHookExecutionHeader;

import java.util.List;
import java.util.function.Consumer;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class WebHookExecution {

  private HttpMethod httpMethod;
  private String url;
  private List<WebHookExecutionHeader> headers;
  private Object payload;
  private MediaType payloadType;
  private Consumer<BaseHttpRequest<?>> prepare;

  public Consumer<BaseHttpRequest<?>> getPrepare() {
    return prepare == null?  request -> {} : prepare;
  }
}
