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

import com.cloudogu.scm.el.ElParser;
import com.cloudogu.scm.el.Expression;
import com.cloudogu.scm.el.env.ImmutableEncodedChangeset;
import com.cloudogu.scm.el.env.ImmutableEncodedRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.Answers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import sonia.scm.repository.Changeset;
import sonia.scm.repository.Repository;
import sonia.scm.webhook.execution.WebHookExecution;

import java.io.IOException;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;
import static org.assertj.core.util.Lists.list;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

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

  @Mock(answer = Answers.RETURNS_SELF)
  private WebhookRequest request;

  @InjectMocks
  @Spy
  private WebHookSender sender;

  private static final List<Changeset> CHANGESETS = list(
    new Changeset("42", 0L, null),
    new Changeset("13", 10L, null),
    new Changeset("6", 33L, null)
  );

  private static final List<WebhookHeader> HEADERS = spy(list(
    new WebhookHeader("thing", "input", false),
    new WebhookHeader("secret", "password", true)
  ));

  private static final List<WebHookExecutionHeader> EXECUTION_HEADERS = list(
    new WebHookExecutionHeader("thing", "input"),
    new WebHookExecutionHeader("secret", "password")
  );
  private MockedStatic<WebHookExecutionHeader> mockedExecutionHeaderStatic;

  @BeforeEach
  void setup() {
    mockedExecutionHeaderStatic = mockStatic(WebHookExecutionHeader.class);
  }

  @AfterEach
  void tearDown() {
    mockedExecutionHeaderStatic.close();
  }

  @Nested
  class AllAtOnce {
    @BeforeEach
    void setup() {
      when(webHook.isExecuteOnEveryCommit()).thenReturn(false);
    }

    @ParameterizedTest
    @EnumSource(value = HttpMethod.class)
    void shouldAttachHeadersToGetRequest(HttpMethod httpMethod) throws IOException {

      mockWebhook("http://test.com", httpMethod, HEADERS, EXECUTION_HEADERS);

      new SimpleWebHookExecutor(sender, elParser, webHook, repository, CHANGESETS).run();

      verifyResult();
    }

    @ParameterizedTest
    @EnumSource(value = HttpMethod.class)
    void shouldSendCommitData(HttpMethod httpMethod) throws IOException {
      mockWebhook("http://test.com", httpMethod, HEADERS, EXECUTION_HEADERS, true);
      when(webHook.isExecuteOnEveryCommit()).thenReturn(false);

      new SimpleWebHookExecutor(sender, elParser, webHook, repository, CHANGESETS).run();

      verifyResult(true);
    }

    @Test
    void shouldNotFailIfChangesetsAreNull() {
      mockWebhook("http://test.com", HttpMethod.AUTO, HEADERS, EXECUTION_HEADERS, true);

      new SimpleWebHookExecutor(sender, elParser, webHook, repository, null).run();

      verify(expression).evaluate(any());
    }
  }

  @Test
  void shouldHandleForEachCommit() {
    mockWebhook("http://test.com", HttpMethod.AUTO, HEADERS, EXECUTION_HEADERS, false);
    when(webHook.isExecuteOnEveryCommit()).thenReturn(true);

    new SimpleWebHookExecutor(sender, elParser, webHook, repository, CHANGESETS).run();

    // Once per changeset
    verify(expression, times(3)).evaluate(argThat(arg -> {
      assertThat(arg.values()).hasSize(3);
      assertThat(arg.values()).allMatch(v -> v instanceof ImmutableEncodedRepository || v instanceof ImmutableEncodedChangeset);
      return true;
    }));
  }

  @Test
  void shouldHandleForEachCommitWithData() {
    mockWebhook("http://test.com", HttpMethod.AUTO, HEADERS, EXECUTION_HEADERS, true, true);
    when(webHook.isExecuteOnEveryCommit()).thenReturn(true);

    new SimpleWebHookExecutor(sender, elParser, webHook, repository, CHANGESETS).run();

    // Once per changeset
    verify(expression, times(3)).evaluate(argThat(arg -> {
      assertThat(arg.values()).hasSize(3);
      assertThat(arg.values()).allMatch(v -> v instanceof ImmutableEncodedRepository || v instanceof ImmutableEncodedChangeset);
      return true;
    }));
  }

  @Test
  void shouldNotFailIfChangesetsAreNullForEachCommit() {
    SimpleWebHook localWebHookMock = mock(SimpleWebHook.class);
    when(localWebHookMock.isExecuteOnEveryCommit()).thenReturn(true);

    new SimpleWebHookExecutor(sender, elParser, localWebHookMock, repository, null).run();

    verifyNoInteractions(expression);
  }

  @Test
  void shouldNotFailIfChangesetIsNull() {
    mockWebhook("http://test.com", HttpMethod.AUTO, HEADERS, EXECUTION_HEADERS, true);

    new SimpleWebHookExecutor(sender, elParser, webHook, repository, null).run();
    verify(expression).evaluate(any());
  }

  void verifyResult() throws IOException {
    verifyResult(false);
  }

  void verifyResult(boolean sendCommitData) throws IOException {
    verify(request).headers(EXECUTION_HEADERS);

    if (sendCommitData) {
      verify(expression).evaluate(argThat(map ->
        map.containsKey("first") && map.containsKey("last") && map.containsKey("repository")
      ));
    }

    verify(request).execute();
    verify(sender).execute(any(WebHookExecution.class));
  }

  void mockWebhook(String url, HttpMethod httpMethod, List<WebhookHeader> headers, List<WebHookExecutionHeader> executionHeaders) {
    mockWebhook(url, httpMethod, headers, executionHeaders, false);
  }

  void mockWebhook(String url, HttpMethod httpMethod, List<WebhookHeader> headers, List<WebHookExecutionHeader> executionHeaders, boolean sendCommitData) {
    mockWebhook(url, httpMethod, headers, executionHeaders, sendCommitData, false);
  }

  void mockWebhook(String url, HttpMethod httpMethod, List<WebhookHeader> headers, List<WebHookExecutionHeader> executionHeaders, boolean sendCommitData, boolean singleCommit) {
    when(webHook.getMethod()).thenReturn(httpMethod);
    when(webHook.getHeaders()).thenReturn(headers);
    when(webHook.isSendCommitData()).thenReturn(sendCommitData);
    when(webHook.getUrlPattern()).thenReturn(url);
    when(elParser.parse(url)).thenReturn(expression);
    when(expression.evaluate(anyMap())).thenReturn(url);

    if (headers.size() != executionHeaders.size()) {
      fail("Size of header list needs to match execution header list.");
    }

    for (int i = 0; i < executionHeaders.size(); i++) {
      int finalI = i;
      mockedExecutionHeaderStatic.when(() -> WebHookExecutionHeader.from(headers.get(finalI))).thenReturn(executionHeaders.get(i));
    }

    switch (httpMethod) {
      case PUT:
        mockRequestWithBody(httpClient::put, url, sendCommitData, singleCommit);
        break;
      case AUTO:
        mockRequestWithBody(httpClient::auto, url, sendCommitData, singleCommit);
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
        when(provider.provide(eq(url), isA(Changeset.class))).thenReturn(request);
      } else {
        when(provider.provide(eq(url), isA(Changesets.class))).thenReturn(request);
      }
    } else {
      when(provider.provide(url, null)).thenReturn(request);
    }
    when(request.headers(anyList())).thenReturn(request);
  }

  private void mockRequest(RequestProvider provider, String url) {
    when(provider.provide(url)).thenReturn(request);
    when(request.headers(anyList())).thenReturn(request);
  }

  private interface RequestWithBodyProvider {
    WebhookRequest provide(String url, Object data);
  }

  private interface RequestProvider {
    WebhookRequest provide(String url);
  }
}
