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
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RepositoryWebHookTest {

  @Mock
  private WebHookContext context;
  private Set<DtoAdapterWebHookSpecification> specifications;

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
      .thenReturn(new WebHookConfiguration(singletonList(new WebHook(new TestWebHookConfiguration(), "42"))));

    hook.handleEvent(event);

    assertThat(specification.executedRepository).isSameAs(repository);
    assertThat(specification.executedEvent).isSameAs(event);
  }

  @Test
  void shouldNotExecuteEventIfRepositoryNotSupportedBySpecification() {
    when(context.getAllConfigurations(repository))
      .thenReturn(new WebHookConfiguration(singletonList(new WebHook(new OtherWebHookConfiguration(), "42"))));

    hook.handleEvent(event);

    // this would fail if OtherWebHookSpecification#createExecutor would have been called
  }

  @Test
  void shouldNotFailCompletelyWithOneMalfunctioningSpecification() {
    when(context.getAllConfigurations(repository))
      .thenReturn(new WebHookConfiguration(singletonList(new WebHook(new MalfunctioningWebHookConfiguration(), "42"))));

    hook.handleEvent(event);

    // no exception should have been thrown
  }

  @Test
  void shouldIgnoreUnknownConfiguration() {
    when(context.getAllConfigurations(repository))
      .thenReturn(new WebHookConfiguration(singletonList(mock(WebHook.class))));

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
