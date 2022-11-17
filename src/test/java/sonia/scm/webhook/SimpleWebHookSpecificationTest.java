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
