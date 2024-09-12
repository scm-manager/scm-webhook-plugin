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

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import jakarta.xml.bind.JAXB;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.Arrays;

import static java.util.Collections.emptyList;
import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class XmlConfigurationTest {

  @Test
  void shouldSerializeAndDeserializeWebHooks() {
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    JAXB.marshal(new WebHookConfiguration(Arrays.asList(
      new WebHook(new SimpleWebHook("https://example.com", true, true, HttpMethod.AUTO, emptyList()), "1"),
      new WebHook(new SimpleWebHook("https://hog/trigger", false, false, HttpMethod.GET, emptyList()), "2")
    )), baos);

    byte[] bytes = baos.toByteArray();

    WebHookConfiguration configuration = JAXB.unmarshal(new ByteArrayInputStream(bytes), WebHookConfiguration.class);

    assertThat(configuration.getWebhooks())
      .extracting("configuration")
      .contains(
        new SimpleWebHook("https://example.com", true, true, HttpMethod.AUTO,emptyList()),
        new SimpleWebHook("https://hog/trigger", false, false, HttpMethod.GET, emptyList())
      );
    assertThat(configuration.getWebhooks())
      .extracting("id")
      .contains("1", "2");
  }
}
