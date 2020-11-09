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
package sonia.scm.webhook.impl;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sonia.scm.net.ahc.AdvancedHttpClient;
import sonia.scm.webhook.HttpMethod;

import java.io.IOException;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AhcWebHookHttpClientTest {

  public static final String URL = "https://scm-manager.org";
  @Mock(answer = Answers.RETURNS_DEEP_STUBS)
  private AdvancedHttpClient ahClient;

  @InjectMocks
  private AhcWebHookHttpClient client;

  @Test
  void shouldUsePostMethodWithObject() throws IOException {
    when(ahClient.method("POST", "https://scm-manager.org").spanKind("Webhook").request().getStatus()).thenReturn(200);

    client.execute(HttpMethod.AUTO, "https://scm-manager.org", "MyData");

    // 2 times, one for the when above
    verify(ahClient, times(2)).method("POST", "https://scm-manager.org");
  }

  @Test
  void shouldUseGetMethodWithoutObject() throws IOException {
    when(ahClient.method("GET", "https://scm-manager.org").spanKind("Webhook").request().getStatus()).thenReturn(400);

    client.execute(HttpMethod.AUTO, "https://scm-manager.org");

    verify(ahClient, times(2)).method("GET", "https://scm-manager.org");
  }

  @Test
  void shouldUseGivenMethod() throws IOException {
    client.execute(HttpMethod.PUT, URL, "data");

    verify(ahClient.method("PUT", URL).spanKind("Webhook")).jsonContent("data");
  }

}
