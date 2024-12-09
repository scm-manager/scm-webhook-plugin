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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sonia.scm.repository.Changeset;
import sonia.scm.repository.PostReceiveRepositoryHookEvent;
import sonia.scm.repository.Repository;
import sonia.scm.repository.RepositoryTestData;
import sonia.scm.repository.api.HookChangesetBuilder;
import sonia.scm.repository.api.HookContext;
import sonia.scm.repository.api.HookFeature;

import java.util.List;

import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SimpleWebHookSpecificationTest {

  @Mock
  private WebhookHttpClient client;

  @Mock
  private ElParser elParser;

  @Mock
  private PostReceiveRepositoryHookEvent event;
  @Mock
  private HookContext eventContext;
  @Mock(answer = Answers.RETURNS_SELF)
  private HookChangesetBuilder changesetBuilder;

  private final Repository repository = RepositoryTestData.createHeartOfGold();

  @InjectMocks
  private WebHookSender sender;

  @Nested
  class EventTest {
    @BeforeEach
    void initEvent() {
      when(event.getContext()).thenReturn(eventContext);
      when(eventContext.isFeatureSupported(HookFeature.CHANGESET_PROVIDER)).thenReturn(true);
      when(eventContext.getChangesetProvider()).thenReturn(changesetBuilder);
      when(changesetBuilder.getChangesets())
        .thenReturn(singletonList(new Changeset("42", 0L, null)));
    }

    @Test
    void shouldGetChangesetsFromEvent() {
      SimpleWebHookSpecification specification = new SimpleWebHookSpecification(sender, elParser);

      SimpleWebHookExecutor executor = (SimpleWebHookExecutor) specification.createExecutor(new SimpleWebHook(), repository, event);

      assertThat(executor).extracting("repository").isSameAs(repository);
      assertThat(executor).extracting("changesets")
        .asList()
        .extracting("id")
        .containsExactly("42");
    }
  }


  @Test
  void shouldEncryptSecretsOnDtoMapping() {
    SimpleWebHook simpleWebHook = new SimpleWebHook();
    simpleWebHook.setHeaders(
      List.of(
        new WebhookHeader("X-token", "mySecret", true),
        new WebhookHeader("simple_header", "no_secret", false)
      )
    );

    SimpleWebHook mappedHook =  new SimpleWebHookSpecification(sender, elParser).mapToDto(simpleWebHook);

    assertThat(mappedHook.getHeaders().get(0).getValue()).isEqualTo("__DUMMY__");
    assertThat(mappedHook.getHeaders().get(1).getValue()).isEqualTo("no_secret");
  }

  @Test
  void shouldRestoreSecretsForDummyBeforeStorage() {
    SimpleWebHook oldSimpleWebHook = new SimpleWebHook();
    oldSimpleWebHook.setHeaders(
      List.of(
        new WebhookHeader("X-token", "mySecret", true)
      )
    );

    SimpleWebHook newSimpleWebHook = new SimpleWebHook();
    newSimpleWebHook.setHeaders(
      List.of(
        new WebhookHeader("X-token", "__DUMMY__", true),
        new WebhookHeader("new token", "secret2", true)
      )
    );

    new SimpleWebHookSpecification(sender, elParser).updateBeforeStore(oldSimpleWebHook, newSimpleWebHook);

    assertThat(newSimpleWebHook.getHeaders().get(0).getValue()).isEqualTo("mySecret");
    assertThat(newSimpleWebHook.getHeaders().get(1).getValue()).isEqualTo("secret2");
  }
}
