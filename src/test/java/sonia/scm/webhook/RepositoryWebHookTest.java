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

import com.google.common.collect.Sets;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sonia.scm.repository.PostReceiveRepositoryHookEvent;
import sonia.scm.repository.Repository;
import sonia.scm.repository.RepositoryTestData;

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

  private final Repository repository = RepositoryTestData.createHeartOfGold();
  private final TestWebHookSpecification specification = new TestWebHookSpecification();

  @BeforeEach
  void initHook() {
    specifications = Sets.newHashSet(specification, new OtherWebHookSpecification(), new MalfunctioningWebHookSpecification());
    hook = new RepositoryWebHook(context, specifications);
  }

  @BeforeEach
  void initEvent() {
    when(event.getRepository()).thenReturn(repository);
  }

  @Test
  void shouldExecuteEventWithCorrectSpecification() {
    when(context.getAllConfigurations(repository))
      .thenReturn(new WebHookConfiguration(singletonList(new WebHook(new TestWebHookConfiguration()))));

    hook.handleEvent(event);

    assertThat(specification.executedRepository).isSameAs(repository);
    assertThat(specification.executedEvent).isSameAs(event);
  }

  @Test
  void shouldNotExecuteEventIfRepositoryNotSupportedBySpecification() {
    when(context.getAllConfigurations(repository))
      .thenReturn(new WebHookConfiguration(singletonList(new WebHook(new OtherWebHookConfiguration()))));

    hook.handleEvent(event);

    // this would fail if OtherWebHookSpecification#createExecutor would have been called
  }

  @Test
  void shouldNotFailCompletelyWithOneMalfunctioningSpecification() {
    when(context.getAllConfigurations(repository))
      .thenReturn(new WebHookConfiguration(singletonList(new WebHook(new MalfunctioningWebHookConfiguration()))));

    hook.handleEvent(event);

    // no exception should have been thrown
  }

  static class TestWebHookConfiguration implements SingleWebHookConfiguration {
  }

  static class TestWebHookSpecification implements WebHookSpecification<TestWebHookConfiguration> {

    TestWebHookConfiguration executedConfiguration;
    Repository executedRepository;
    PostReceiveRepositoryHookEvent executedEvent;

    @Override
    public Class<TestWebHookConfiguration> getSpecificationType() {
      return TestWebHookConfiguration.class;
    }

    @Override
    public WebHookExecutor createExecutor(TestWebHookConfiguration webHook, Repository repository, PostReceiveRepositoryHookEvent event) {
      return () -> {
        this.executedConfiguration = webHook;
        this.executedRepository = repository;
        this.executedEvent = event;
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
    public boolean supportsRepository(Repository repository) {
      return false;
    }

    @Override
    public WebHookExecutor createExecutor(OtherWebHookConfiguration webHook, Repository repository, PostReceiveRepositoryHookEvent changesets) {
      fail("this should not have been called");
      return null;
    }
  }

  static class MalfunctioningWebHookConfiguration implements SingleWebHookConfiguration {
  }

  static class MalfunctioningWebHookSpecification implements WebHookSpecification<MalfunctioningWebHookConfiguration> {

    @Override
    public Class<MalfunctioningWebHookConfiguration> getSpecificationType() {
      return MalfunctioningWebHookConfiguration.class;
    }

    @Override
    public WebHookExecutor createExecutor(MalfunctioningWebHookConfiguration webHook, Repository repository, PostReceiveRepositoryHookEvent changesets) {
      throw new RuntimeException("test");
    }
  }
}
