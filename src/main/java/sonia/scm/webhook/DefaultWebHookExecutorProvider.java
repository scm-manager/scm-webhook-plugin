package sonia.scm.webhook;

import com.cloudogu.scm.el.ElParser;
import com.google.inject.Inject;
import com.google.inject.Provider;
import sonia.scm.plugin.Extension;
import sonia.scm.repository.Changeset;
import sonia.scm.repository.Repository;

@Extension
public class DefaultWebHookExecutorProvider implements WebHookExecutorProvider<DefaultWebHook> {

  private final Provider<WebHookHttpClient> httpClientProvider;
  private final ElParser elParser;

  @Inject
  public DefaultWebHookExecutorProvider(Provider<WebHookHttpClient> httpClientProvider, ElParser elParser) {
    this.httpClientProvider = httpClientProvider;
    this.elParser = elParser;
  }

  @Override
  public boolean handles(Class<WebHook> webHookClass) {
    return DefaultWebHook.class.isAssignableFrom(webHookClass);
  }

  @Override
  public WebHookExecutor createExecutor(DefaultWebHook webHook, Repository repository, Iterable<Changeset> changesets) {
    return new DefaultWebHookExecutor(httpClientProvider.get(), elParser, webHook, repository, changesets);
  }
}
