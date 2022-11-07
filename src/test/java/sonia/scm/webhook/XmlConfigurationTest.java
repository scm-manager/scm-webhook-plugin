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

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.xml.bind.JAXB;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class XmlConfigurationTest {

  @Test
  void shouldSerializeAndDeserializeWebHooks() {
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    JAXB.marshal(new WebHookConfiguration(Arrays.asList(
      new WebHook(new SimpleWebHook("https://example.com", true, true, HttpMethod.AUTO)),
      new WebHook(new SimpleWebHook("https://hog/trigger", false, false, HttpMethod.GET))
    )), baos);

    byte[] bytes = baos.toByteArray();

    WebHookConfiguration configuration = JAXB.unmarshal(new ByteArrayInputStream(bytes), WebHookConfiguration.class);

    assertThat(configuration.getWebhooks())
      .extracting("configuration")
      .contains(
        new SimpleWebHook("https://example.com", true, true, HttpMethod.AUTO),
        new SimpleWebHook("https://hog/trigger", false, false, HttpMethod.GET)
      );
  }
}
