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
import com.google.inject.Provider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sonia.scm.repository.Changeset;
import sonia.scm.repository.PostReceiveRepositoryHookEvent;
import sonia.scm.repository.Repository;
import sonia.scm.repository.RepositoryTestData;
import sonia.scm.repository.api.HookChangesetBuilder;
import sonia.scm.repository.api.HookContext;
import sonia.scm.repository.api.HookFeature;

import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SimpleWebHookSpecificationTest {

  @Mock
  private Provider<WebHookHttpClient> httpClientProvider;
  @Mock
  private ElParser elParser;

  @Mock
  private PostReceiveRepositoryHookEvent event;
  @Mock
  private HookContext eventContext;
  @Mock(answer = Answers.RETURNS_SELF)
  private HookChangesetBuilder changesetBuilder;

  private final Repository repository = RepositoryTestData.createHeartOfGold();

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
    SimpleWebHookSpecification specification = new SimpleWebHookSpecification(httpClientProvider, elParser);

    SimpleWebHookExecutor executor = (SimpleWebHookExecutor) specification.createExecutor(new SimpleWebHook(), repository, event);

    assertThat(executor).extracting("repository").isSameAs(repository);
    assertThat(executor).extracting("changesets")
      .asList()
      .extracting("id")
      .containsExactly("42");
  }
}
