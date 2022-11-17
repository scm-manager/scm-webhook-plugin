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

import com.github.sdorra.shiro.ShiroRule;
import com.github.sdorra.shiro.SubjectAware;
import com.google.inject.Provider;
import com.google.inject.util.Providers;
import de.otto.edison.hal.HalRepresentation;
import org.apache.shiro.util.ThreadContext;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import sonia.scm.api.v2.resources.HalAppender;
import sonia.scm.api.v2.resources.HalEnricherContext;
import sonia.scm.api.v2.resources.ScmPathInfoStore;
import sonia.scm.repository.Repository;

import java.net.URI;
import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SubjectAware(configuration = "classpath:sonia/scm/webhook/internal/shiro.ini")
@RunWith(MockitoJUnitRunner.class)
public class RepositoryLinkEnricherTest {

  private Provider<ScmPathInfoStore> scmPathInfoStoreProvider;

  @Rule
  public ShiroRule shiro = new ShiroRule();

  @Mock
  private HalAppender appender;
  @Mock
  private AvailableWebHookSpecifications availableSpecifications;
  private RepositoryLinkEnricher enricher;

  private final Repository repo = new Repository("id", "type", "space", "name");

  public RepositoryLinkEnricherTest() {
    // cleanup state that might have been left by other tests
    ThreadContext.unbindSecurityManager();
    ThreadContext.unbindSubject();
    ThreadContext.remove();
  }

  @Before
  public void setUp() {
    ScmPathInfoStore scmPathInfoStore = new ScmPathInfoStore();
    scmPathInfoStore.set(() -> URI.create("https://scm-manager.org/scm/api/"));
    scmPathInfoStoreProvider = Providers.of(scmPathInfoStore);
    enricher = new RepositoryLinkEnricher(scmPathInfoStoreProvider, availableSpecifications);
  }

  @Test
  @SubjectAware( username = "trillian", password = "secret")
  public void shouldEnrichIndex() {
    HalEnricherContext context = HalEnricherContext.of(repo);

    enricher.enrich(context, appender);

    verify(appender).appendLink("webHookConfig", "https://scm-manager.org/scm/api/v2/plugins/webhook/space/name");
  }

  @Test
  @SubjectAware(username = "unpriv", password = "secret")
  public void shouldNotEnrichIndexBecauseOfMissingPermission() {
    HalEnricherContext context = HalEnricherContext.of(repo);

    enricher.enrich(context, appender);

    verify(appender, never()).appendLink("webHookConfig", "https://scm-manager.org/scm/api/v2/plugins/webhook/space/name");
  }

  @Test
  @SubjectAware(username = "trillian", password = "secret")
  public void shouldEnrichEmbeddedWithTypes() {
    HalEnricherContext context = HalEnricherContext.of(repo);
    ArgumentCaptor<HalRepresentation> captor = ArgumentCaptor.forClass(HalRepresentation.class);
    doNothing().when(appender).appendEmbedded(eq("supportedWebHookTypes"), captor.capture());
    when(availableSpecifications.getTypesFor(repo)).thenReturn(Arrays.asList("simple", "complex"));

    enricher.enrich(context, appender);

    assertThat(captor.getValue()).extracting("types").asList().contains("simple", "complex");
  }
}
