package sonia.scm.webhook;

import com.github.sdorra.shiro.ShiroRule;
import com.github.sdorra.shiro.SubjectAware;
import com.google.inject.Provider;
import com.google.inject.util.Providers;
import org.apache.shiro.util.ThreadContext;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import sonia.scm.api.v2.resources.HalAppender;
import sonia.scm.api.v2.resources.HalEnricherContext;
import sonia.scm.api.v2.resources.ScmPathInfoStore;

import java.net.URI;

import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@SubjectAware(configuration = "classpath:sonia/scm/webhook/internal/shiro.ini")
@RunWith(MockitoJUnitRunner.class)
public class IndexLinkEnricherTest {

  private Provider<ScmPathInfoStore> scmPathInfoStoreProvider;

  @Rule
  public ShiroRule shiro = new ShiroRule();

  @Mock
  private HalAppender appender;
  private IndexLinkEnricher enricher;

  public IndexLinkEnricherTest() {
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
  }

  @Test
  @SubjectAware( username = "trillian", password = "secret")
  public void shouldEnrichIndex() {
    enricher = new IndexLinkEnricher(scmPathInfoStoreProvider);
    enricher.enrich(HalEnricherContext.of(), appender);
    verify(appender).appendLink("webHookConfig", "https://scm-manager.org/scm/api/v2/plugins/webhook");
  }

  @Test
  @SubjectAware(username = "unpriv", password = "secret")
  public void shouldNotEnrichIndexBecauseOfMissingPermission() {
    enricher = new IndexLinkEnricher(scmPathInfoStoreProvider);
    enricher.enrich(HalEnricherContext.of(), appender);
    verify(appender, never()).appendLink("webHookConfig", "https://scm-manager.org/scm/api/v2/plugins/webhook");
  }

}