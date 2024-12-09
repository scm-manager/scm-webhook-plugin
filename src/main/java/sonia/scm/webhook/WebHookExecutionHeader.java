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

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import sonia.scm.net.ahc.Content;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class WebHookExecutionHeader {
  private String key;
  private Function<Content, String> valueProvider;

  public WebHookExecutionHeader(String key, String value) {
    this(key, c -> value);
  }

  public WebHookExecutionHeader(String key, Supplier<String> valueProvider) {
    this(key, c -> valueProvider.get());
  }

  public void setValue(String value) {
    this.setValueProvider(() -> value);
  }

  public void setValueProvider(Supplier<String> valueProvider) {
    this.valueProvider = c -> valueProvider.get();
  }

  public void setValueProvider(Function<Content, String> valueProvider) {
    this.valueProvider = valueProvider;
  }

  public String getValue() {
    try {
      return valueProvider.apply(null);
    } catch (Exception e) {
      throw new WebHookHeaderExecutionException("Operation likely not supported due to valueProvider with null content mismatch", e);
    }
  }

  public String getValue(Content content) {
    try {
      return valueProvider.apply(content);
    } catch (Exception e) {
      throw new WebHookHeaderExecutionException("Content transformer in webhook threw unexpected exception", e);
    }
  }

  public static WebHookExecutionHeader from(WebhookHeader header) {
    return new WebHookExecutionHeader(header.getKey(), header.getValue());
  }

  public static List<WebHookExecutionHeader> from(List<WebhookHeader> headers) {
    return headers.stream().map(WebHookExecutionHeader::from).toList();
  }
}
