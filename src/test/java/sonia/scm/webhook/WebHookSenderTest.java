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

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.Answers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sonia.scm.net.ahc.AdvancedHttpRequestWithBody;
import sonia.scm.net.ahc.BaseHttpRequest;
import sonia.scm.webhook.execution.WebHookExecution;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class WebHookSenderTest {

  @Mock(answer = Answers.RETURNS_DEEP_STUBS)
  AdvancedHttpRequestWithBody internalRequest;

  WebhookRequest exampleWebHookRequest;

  @InjectMocks
  WebHookSender target;

  @Mock
  WebhookHttpClient webhookHttpClient;

  @BeforeEach
  void setUp() {
    exampleWebHookRequest = spy(new WebhookRequest(internalRequest));
  }

  @Test
  void shouldBuildCorrectRequestAndSend() {
    List<WebHookExecutionHeader> headers = List.of(
      new WebHookExecutionHeader("key1", "value1"),
      new WebHookExecutionHeader("key2", "value2")
    );
    when(webhookHttpClient.auto(any(), any())).thenReturn(exampleWebHookRequest);
    String payload = "payload";
    String url = "url";
    WebHookExecution execution = WebHookExecution
      .builder()
      .httpMethod(HttpMethod.AUTO)
      .url(url)
      .headers(headers)
      .payload(payload)
      .build();

    target.execute(execution);

    verify(webhookHttpClient).auto(url, payload);
    verify(exampleWebHookRequest).headers(headers);
    assertDoesNotThrow(verify(exampleWebHookRequest)::execute);
  }

  @ParameterizedTest
  @EnumSource(HttpMethod.class)
  void shouldApplyFunctionFromPreparableWebHookExecution(HttpMethod method) {
    mockByHttpMethod(method);

    Supplier<String> headerValueTransformer = () -> "Hooray";
    Consumer<BaseHttpRequest<?>> prepareRequest = request -> {
      request.header("isPrepared", "true");
      request.disableCertificateValidation(true);
    };
    WebHookExecutionHeader header = new WebHookExecutionHeader("key1", headerValueTransformer);
    String payload = "fancyPayload";
    WebHookExecution webHookExecution = WebHookExecution.builder()
      .httpMethod(method)
      .url("http://example.org")
      .headers(List.of(header))
      .payload(payload)
      .prepare(prepareRequest)
      .build();

    target.execute(webHookExecution);

    verifyCallByMethod(method, "http://example.org", payload);
    verify(exampleWebHookRequest).accept(prepareRequest);
    verify(internalRequest).header("isPrepared", "true");
    verify(internalRequest).disableCertificateValidation(true);
  }

  private void verifyCallByMethod(HttpMethod method, String url, Object payload) {
    switch (method) {
      case AUTO:
        verify(webhookHttpClient).auto(url, payload);
        break;
      case GET:
        verify(webhookHttpClient).get(url);
        break;
      case POST:
        verify(webhookHttpClient).post(url, payload);
        break;
      case PUT:
        verify(webhookHttpClient).put(url, payload);
        break;
    }
  }

  private void mockByHttpMethod(HttpMethod method) {
    switch (method) {
      case AUTO:
        when(webhookHttpClient.auto(any(), any())).thenReturn(exampleWebHookRequest);
        break;
      case GET:
        when(webhookHttpClient.get(any())).thenReturn(exampleWebHookRequest);
        break;
      case POST:
        when(webhookHttpClient.post(any(), any())).thenReturn(exampleWebHookRequest);
        break;
      case PUT:
        when(webhookHttpClient.put(any(), any())).thenReturn(exampleWebHookRequest);
    }
  }

}
