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
import org.mockito.Answers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sonia.scm.net.ahc.AdvancedHttpClient;
import sonia.scm.net.ahc.AdvancedHttpRequest;
import sonia.scm.net.ahc.AdvancedHttpRequestWithBody;
import sonia.scm.net.ahc.AdvancedHttpResponse;

import jakarta.inject.Provider;
import java.io.IOException;

import static org.mockito.Mockito.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class WebhookHttpClientTest {

  public static final String URL = "https://scm-manager.org";
  @Mock
  private Provider<AdvancedHttpClient> clientProvider;
  @Mock
  private AdvancedHttpClient advancedHttpClient;

  @Mock(answer = Answers.RETURNS_SELF)
  private AdvancedHttpRequest request;

  @Mock
  private AdvancedHttpResponse response;

  @Mock(answer = Answers.RETURNS_SELF)
  private AdvancedHttpRequestWithBody requestWithBody;

  @InjectMocks
  private WebhookHttpClient client;

  @BeforeEach
  void initClient() throws IOException {
    when(clientProvider.get()).thenReturn(advancedHttpClient);
    lenient().when(advancedHttpClient.get(any())).thenReturn(request);
    lenient().when(advancedHttpClient.put(any())).thenReturn(requestWithBody);
    lenient().when(advancedHttpClient.post(any())).thenReturn(requestWithBody);
    lenient().when(response.getStatus()).thenReturn(200);
    lenient().when(request.request()).thenReturn(response);
    lenient().when(requestWithBody.request()).thenReturn(response);
  }

  @Test
  void shouldUsePostMethodForAutoWithData() throws IOException {
    client.auto(URL, "MyData").execute();

    verify(advancedHttpClient, times(1)).post(URL);
    verify(requestWithBody).jsonContent("MyData");
    verify(requestWithBody).spanKind("Webhook");
  }

  @Test
  void shouldUseGetMethodForAutoWithoutBody() throws IOException {
    client.auto(URL, null).execute();

    verify(advancedHttpClient, times(1)).get("https://scm-manager.org");
    verify(request).spanKind("Webhook");
  }

  @Test
  void shouldPerformPostRequest() throws IOException {
    client.post(URL, "myData").execute();

    verify(advancedHttpClient).post(URL);
    verify(requestWithBody).jsonContent("myData");
    verify(requestWithBody).spanKind("Webhook");
  }

  @Test
  void shouldPerformPostRequestWithoutBody() throws IOException {
    client.post(URL, null).execute();

    verify(advancedHttpClient).post(URL);
    verify(requestWithBody, never()).jsonContent(any());
    verify(requestWithBody).spanKind("Webhook");
  }

  @Test
  void shouldPerformPutRequest() throws IOException {
    client.put(URL, "myData").execute();

    verify(advancedHttpClient).put(URL);
    verify(requestWithBody).jsonContent("myData");
    verify(requestWithBody).spanKind("Webhook");
  }

  @Test
  void shouldPerformPutRequestWithoutBody() throws IOException {
    client.put(URL, null).execute();

    verify(advancedHttpClient).put(URL);
    verify(requestWithBody, never()).jsonContent(any());
    verify(requestWithBody).spanKind("Webhook");
  }

  @Test
  void shouldPerformGetRequest() throws IOException {
    client.get(URL).execute();

    verify(advancedHttpClient).get(URL);
    verify(request).spanKind("Webhook");
  }
}
