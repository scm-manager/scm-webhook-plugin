package sonia.scm.webhook;

import sonia.scm.api.v2.resources.Enrich;
import sonia.scm.api.v2.resources.LinkAppender;
import sonia.scm.api.v2.resources.LinkEnricher;
import sonia.scm.api.v2.resources.LinkEnricherContext;
import sonia.scm.api.v2.resources.ScmPathInfoStore;
import sonia.scm.plugin.Extension;
import sonia.scm.repository.Repository;
import sonia.scm.webhook.internal.WebHookConfigurationResourceLinks;

import javax.inject.Inject;
import javax.inject.Provider;

@Extension
@Enrich(Repository.class)
public class RepositoryLinkEnricher implements LinkEnricher {

  private final Provider<ScmPathInfoStore> scmPathInfoStoreProvider;

  @Inject
  public RepositoryLinkEnricher(Provider<ScmPathInfoStore> scmPathInfoStoreProvider) {
    this.scmPathInfoStoreProvider = scmPathInfoStoreProvider;
  }

  @Override
  public void enrich(LinkEnricherContext context, LinkAppender appender) {
    if (WebHookContext.isReadPermitted()) {
      Repository repository = context.oneRequireByType(Repository.class);
      WebHookConfigurationResourceLinks resourceLinks = new WebHookConfigurationResourceLinks(scmPathInfoStoreProvider.get().get());
      appender.appendOne("webHookConfig", resourceLinks.repositoryConfigurations.self(repository.getNamespace(), repository.getName()));
    }
  }
}
