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

import javax.inject.Provider;
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
