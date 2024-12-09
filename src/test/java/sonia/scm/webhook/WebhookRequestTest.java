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

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sonia.scm.net.ahc.AdvancedHttpRequestWithBody;
import sonia.scm.net.ahc.BaseHttpRequest;
import sonia.scm.net.ahc.Content;
import sonia.scm.net.ahc.RawContent;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class WebhookRequestTest {

  @Mock(answer = Answers.RETURNS_DEEP_STUBS)
  AdvancedHttpRequestWithBody advancedRequest;

  @Mock(answer = Answers.RETURNS_DEEP_STUBS)
  BaseHttpRequest<?> baseRequest;

  Content exampleContent;

  @Nested
  class Headers {
    void prepareAdvancedRequest() {
      exampleContent = spy(new RawContent("Hitchhiker".getBytes()));
      when(advancedRequest.getContent()).thenReturn(exampleContent);
    }



    @Test
    void shouldPerformContentTransformerCallOnPost() throws IOException {
      ArgumentCaptor<String> headerKeyCaptor = ArgumentCaptor.forClass(String.class);
      ArgumentCaptor<String> headerValueCaptor = ArgumentCaptor.forClass(String.class);

      prepareAdvancedRequest();
      WebhookRequest webhookRequest = new WebhookRequestWithContent(advancedRequest);

      WebHookExecutionHeader header = new WebHookExecutionHeader("someKey", c -> {
        try(ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
          c.process(baos);
          return baos.toString();
        } catch (IOException e) {
          fail("Unexpected output stream exception, please check test structure.", e);
        }
        return "";
      });

      webhookRequest.headers(List.of(header));

      // It can be any class. If the ByteArrayOutputStream causes problems, it can be changed.
      verify(exampleContent).process(any(ByteArrayOutputStream.class));
      verify(advancedRequest).header(headerKeyCaptor.capture(), headerValueCaptor.capture());

      assertEquals("someKey", headerKeyCaptor.getValue());
      assertEquals("Hitchhiker", headerValueCaptor.getValue());
    }

    @Test
    void shouldGetValueFromHeaderOnGet() {
      ArgumentCaptor<String> headerKeyCaptor = ArgumentCaptor.forClass(String.class);
      ArgumentCaptor<String> headerValueCaptor = ArgumentCaptor.forClass(String.class);

      WebhookRequest webhookRequest = new WebhookRequest(baseRequest);

      WebHookExecutionHeader header = new WebHookExecutionHeader("someKey", "someValue");

      webhookRequest.headers(List.of(header));

      verify(baseRequest).header(headerKeyCaptor.capture(), headerValueCaptor.capture());

      assertEquals("someKey", headerKeyCaptor.getValue());
      assertEquals("someValue", headerValueCaptor.getValue());
    }
  }
}
