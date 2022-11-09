package sonia.scm.webhook;

import com.google.common.collect.Sets;
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

import java.util.Set;

import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RepositoryWebHookTest {

  @Mock
  private WebHookContext context;
  private Set<WebHookSpecification> specifications;

  private RepositoryWebHook hook;

  @Mock
  private PostReceiveRepositoryHookEvent event;
  @Mock
  private HookContext eventContext;
  @Mock(answer = Answers.RETURNS_SELF)
  private HookChangesetBuilder changesetBuilder;

  private final Repository repository = RepositoryTestData.createHeartOfGold();
  private final TestWebHookSpecification specification = new TestWebHookSpecification();

  @BeforeEach
  void initHook() {
    specifications = Sets.newHashSet(specification, new OtherWebHookSpecification());
    hook = new RepositoryWebHook(context, specifications);
  }

  @BeforeEach
  void initEvent() {
    when(event.getRepository()).thenReturn(repository);
    when(event.getContext()).thenReturn(eventContext);
    when(eventContext.isFeatureSupported(HookFeature.CHANGESET_PROVIDER)).thenReturn(true);
    when(eventContext.getChangesetProvider()).thenReturn(changesetBuilder);
    when(changesetBuilder.getChangesets())
      .thenReturn(singletonList(new Changeset("23", 0L, null)));
  }

  @BeforeEach
  void initWebHookContext() {
    when(context.getAllConfigurations(repository))
      .thenReturn(new WebHookConfiguration(singletonList(new WebHook(new TestWebHookConfiguration()))));
  }

  @Test
  void shouldExecuteEventWithCorrectSpecification() {
    hook.handleEvent(event);

    assertThat(specification.executedRepository).isSameAs(repository);
    assertThat(specification.executedChangesets).extracting("id").contains("23");
  }

  static class TestWebHookConfiguration implements SingleWebHookConfiguration {
  }

  static class TestWebHookSpecification implements WebHookSpecification<TestWebHookConfiguration> {

    TestWebHookConfiguration executedConfiguration;
    Repository executedRepository;
    Iterable<Changeset> executedChangesets;

    @Override
    public Class<TestWebHookConfiguration> getSpecificationType() {
      return TestWebHookConfiguration.class;
    }

    @Override
    public WebHookExecutor createExecutor(TestWebHookConfiguration webHook, Repository repository, Iterable<Changeset> changesets) {
      return () -> {
        this.executedConfiguration = webHook;
        this.executedRepository = repository;
        this.executedChangesets = changesets;
      };
    }
  }

  static class OtherWebHookConfiguration implements SingleWebHookConfiguration {
  }

  static class OtherWebHookSpecification implements WebHookSpecification<OtherWebHookConfiguration> {

    @Override
    public Class<OtherWebHookConfiguration> getSpecificationType() {
      return OtherWebHookConfiguration.class;
    }

    @Override
    public WebHookExecutor createExecutor(OtherWebHookConfiguration webHook, Repository repository, Iterable<Changeset> changesets) {
      fail("this should not have been called");
      return null;
    }
  }
}
