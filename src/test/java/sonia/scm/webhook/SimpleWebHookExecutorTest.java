/*
 * MIT License
 *
 * Copyright (c) 2020-present Cloudogu GmbH and Contributors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package sonia.scm.webhook;

import com.cloudogu.scm.el.ElParser;
import com.cloudogu.scm.el.Expression;
import com.cloudogu.scm.el.env.ImmutableEncodedChangeset;
import com.cloudogu.scm.el.env.ImmutableEncodedRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sonia.scm.net.ahc.AdvancedHttpRequest;
import sonia.scm.net.ahc.AdvancedHttpRequestWithBody;
import sonia.scm.net.ahc.BaseHttpRequest;
import sonia.scm.repository.Changeset;
import sonia.scm.repository.Repository;

import java.io.IOException;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.util.Lists.list;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SimpleWebHookExecutorTest {

  @Mock
  private WebhookHttpClient httpClient;

  @Mock
  private ElParser elParser;

  @Mock
  private SimpleWebHook webHook;

  @Mock
  private Repository repository;

  @Mock
  private Expression expression;

  @Mock
  private WebhookRequest<AdvancedHttpRequestWithBody> requestWithBody;

  @Mock
  private WebhookRequest<AdvancedHttpRequest> request;

  private static final List<Changeset> CHANGESETS = list(
    new Changeset("42", 0L, null),
    new Changeset("13", 10L, null),
    new Changeset("6", 33L, null)
  );

  private static final List<WebhookHeader> HEADERS = list(
    new WebhookHeader("thing", "input", false),
    new WebhookHeader("secret", "password", true)
  );

  @Nested
  class AllAtOnce {
    @BeforeEach
    void setup() {
      when(webHook.isExecuteOnEveryCommit()).thenReturn(false);
    }

    @ParameterizedTest
    @EnumSource(value = HttpMethod.class)
    void shouldAttachHeadersToGetRequest(HttpMethod httpMethod) throws IOException {

      mockWebhook("http://test.com", httpMethod, HEADERS);

      new SimpleWebHookExecutor(httpClient, elParser, webHook, repository, CHANGESETS).run();

      verifyResult(httpMethod);
    }

    @ParameterizedTest
    @EnumSource(value = HttpMethod.class)
    void shouldSendCommitData(HttpMethod httpMethod) throws IOException {
      mockWebhook("http://test.com", httpMethod, HEADERS, true);

      new SimpleWebHookExecutor(httpClient, elParser, webHook, repository, CHANGESETS).run();

      verifyResult(httpMethod, true);
    }

    @Test
    void shouldNotFailIfChangesetsAreNull() {
      mockWebhook("http://test.com", HttpMethod.AUTO, HEADERS, true);

      new SimpleWebHookExecutor(httpClient, elParser, webHook, repository, null).run();

      verify(expression).evaluate(any());
    }
  }

  @Test
  void shouldHandleForEachCommit() {
    mockWebhook("http://test.com", HttpMethod.AUTO, HEADERS, false);
    when(webHook.isExecuteOnEveryCommit()).thenReturn(true);

    new SimpleWebHookExecutor(httpClient, elParser, webHook, repository, CHANGESETS).run();

    // Once per changeset
    verify(expression, times(3)).evaluate(argThat(arg -> {
      assertThat(arg.values()).hasSize(3);
      assertThat(arg.values()).allMatch(v -> v instanceof ImmutableEncodedRepository || v instanceof ImmutableEncodedChangeset);
      return true;
    }));
  }

  @Test
  void shouldHandleForEachCommitWithData() {
    mockWebhook("http://test.com", HttpMethod.AUTO, HEADERS, true);
    when(webHook.isExecuteOnEveryCommit()).thenReturn(true);

    new SimpleWebHookExecutor(httpClient, elParser, webHook, repository, CHANGESETS).run();

    // Once per changeset
    verify(expression, times(3)).evaluate(argThat(arg -> {
      assertThat(arg.values()).hasSize(3);
      assertThat(arg.values()).allMatch(v -> v instanceof ImmutableEncodedRepository || v instanceof ImmutableEncodedChangeset);
      return true;
    }));
  }

  @Test
  void shouldNotFailIfChangesetsAreNullForEachCommit() {
    SimpleWebHook webHook = mock(SimpleWebHook.class);
    when(webHook.isExecuteOnEveryCommit()).thenReturn(true);

    new SimpleWebHookExecutor(httpClient, elParser, webHook, repository, null).run();

    verifyNoInteractions(expression);
  }

  @Test
  void shouldNotFailIfChangesetIsNull() {
    mockWebhook("http://test.com", HttpMethod.AUTO, HEADERS, true, true);

    new SimpleWebHookExecutor(httpClient, elParser, webHook, repository, null).run();
    verify(expression).evaluate(any());
  }

  void verifyResult(HttpMethod httpMethod) throws IOException {
    verifyResult(httpMethod, false);
  }

  void verifyResult(HttpMethod httpMethod, boolean sendCommitData) throws IOException {
    verifyResult(httpMethod, sendCommitData, false);
  }

  void verifyResult(HttpMethod httpMethod, boolean sendCommitData, boolean singleCommit) throws IOException {
    WebhookRequest<? extends BaseHttpRequest<?>> webhookRequest = getRequest(httpMethod, sendCommitData);

    verify(webhookRequest).headers(HEADERS);

    if (sendCommitData) {
      if (singleCommit) {
        verify(expression, times(3)).evaluate(argThat(map ->
          map.containsKey("changeset") && map.containsKey("commit") && map.containsKey("repository")
        ));
      } else {
        verify(expression).evaluate(argThat(map ->
          map.containsKey("first") && map.containsKey("last") && map.containsKey("repository")
        ));
      }
    }

    if (singleCommit) {
      verify(webhookRequest, times(3)).execute();
    } else {
      verify(webhookRequest).execute();
    }
  }

  void mockWebhook(String url, HttpMethod httpMethod, List<WebhookHeader> headers) {
    mockWebhook(url, httpMethod, headers, false);
  }

  void mockWebhook(String url, HttpMethod httpMethod, List<WebhookHeader> headers, boolean sendCommitData) {
    mockWebhook(url, httpMethod, headers, sendCommitData, false);
  }

  void mockWebhook(String url, HttpMethod httpMethod, List<WebhookHeader> headers, boolean sendCommitData, boolean singleCommit) {
    when(webHook.getMethod()).thenReturn(httpMethod);
    when(webHook.getHeaders()).thenReturn(headers);
    when(webHook.isSendCommitData()).thenReturn(sendCommitData);
    when(webHook.getUrlPattern()).thenReturn(url);
    when(elParser.parse(url)).thenReturn(expression);
    when(expression.evaluate(anyMap())).thenReturn(url);

    switch (httpMethod) {
      case PUT:
        mockRequestWithBody(httpClient::put, url, sendCommitData, singleCommit);
        break;
      case AUTO:
        mockAuto(url, sendCommitData, singleCommit);
        break;
      case POST:
        mockRequestWithBody(httpClient::post, url, sendCommitData, singleCommit);
        break;
      case GET:
        mockRequest(httpClient::get, url);
        break;
    }
  }

  private void mockRequestWithBody(RequestWithBodyProvider provider, String url, boolean sendCommitData, boolean singleCommit) {
    if (sendCommitData) {
      if (singleCommit) {
        when(provider.provide(eq(url), isA(Changeset.class))).thenReturn(requestWithBody);
      } else {
        when(provider.provide(eq(url), isA(Changesets.class))).thenReturn(requestWithBody);
      }
    } else {
      when(provider.provide(url, null)).thenReturn(requestWithBody);
    }
    when(requestWithBody.headers(anyList())).thenReturn(requestWithBody);
  }

  private void mockRequest(RequestProvider provider, String url) {
    when(provider.provide(url)).thenReturn(request);
    when(request.headers(anyList())).thenReturn(request);
  }

  private void mockAuto(String url, boolean sendCommitData, boolean singleCommit) {
    if (sendCommitData) {
      mockRequestWithBody(((a, b) -> (WebhookRequest<AdvancedHttpRequestWithBody>) httpClient.auto(a, b)), url, sendCommitData, singleCommit);
    } else {
      mockRequest((a -> (WebhookRequest<AdvancedHttpRequest>) httpClient.auto(a, null)), url);
    }
  }

  private interface RequestWithBodyProvider {
    WebhookRequest<AdvancedHttpRequestWithBody> provide(String url, Object data);
  }

  private interface RequestProvider {
    WebhookRequest<AdvancedHttpRequest> provide(String url);
  }

  WebhookRequest<? extends BaseHttpRequest<?>> getRequest(HttpMethod httpMethod, boolean sendCommitData) {
    if (httpMethod == HttpMethod.GET || (httpMethod == HttpMethod.AUTO && !sendCommitData)) {
      return request;
    }
    return requestWithBody;
  }

}
