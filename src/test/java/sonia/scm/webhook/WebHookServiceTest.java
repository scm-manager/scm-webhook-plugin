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
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sonia.scm.repository.Repository;
import sonia.scm.repository.RepositoryTestData;
import sonia.scm.webhook.execution.WebHookExecution;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class WebHookServiceTest {

  @Mock
  WebHookContext context;

  @Mock
  WebHookSender sender;

  @InjectMocks
  WebHookService target;

  private final Repository repository = RepositoryTestData.createHeartOfGold();

  @Test
  void shouldProvideConfigurations() {
    GoodWebHookConfiguration goodConfig1 = new GoodWebHookConfiguration();
    GoodWebHookConfiguration goodConfig2 = new GoodWebHookConfiguration();
    BadWebHookConfiguration badConfig = new BadWebHookConfiguration();

    WebHook webHook1 = new WebHook(goodConfig1, "id1");
    WebHook webHook2 = new WebHook(goodConfig2, "id2");
    WebHook webHook3 = new WebHook(badConfig, "id3");

    when(context.getAllConfigurations(repository))
      .thenReturn(new WebHookConfiguration(List.of(webHook1, webHook3, webHook2)));

    List<GoodWebHookConfiguration> configurations = target.getConfigurations(GoodWebHookConfiguration.class, repository);
    assertThat(configurations).hasSize(2);
  }

  @Test
  void shouldProvideEmptyConfigurationsGivenNoSuitableInstances() {
    BadWebHookConfiguration badConfig = new BadWebHookConfiguration();

    WebHook webHook = new WebHook(badConfig, "id1");

    when(context.getAllConfigurations(repository)).thenReturn(new WebHookConfiguration(List.of(webHook)));

    List<GoodWebHookConfiguration> configurations = target.getConfigurations(GoodWebHookConfiguration.class, repository);
    assertThat(configurations).isEmpty();
  }

  @Test
  void shouldSendWebhookExecution() {
    WebHookExecution thinWebHookExecution =
      WebHookExecution.builder()
        .httpMethod(HttpMethod.POST)
        .url("http://abc.de")
        .headers(List.of(new WebHookExecutionHeader("a", "b")))
        .build();

    target.execute(thinWebHookExecution);

    verify(sender).execute(thinWebHookExecution);
  }

  class GoodWebHookConfiguration implements SingleWebHookConfiguration {
  }

  class BadWebHookConfiguration implements SingleWebHookConfiguration {
  }
}
