package sonia.scm.webhook.internal;

import sonia.scm.api.v2.resources.LinkBuilder;
import sonia.scm.api.v2.resources.ScmPathInfo;

public class WebHookConfigurationResourceLinks {
  private final LinkBuilder linkBuilder;
  public final GlobalConfigurationLinks globalConfigurations = new GlobalConfigurationLinks();
  public final RepositoryConfigurationLinks repositoryConfigurations = new RepositoryConfigurationLinks();

  public WebHookConfigurationResourceLinks(ScmPathInfo scmPathInfo) {
    this.linkBuilder = new LinkBuilder(scmPathInfo, WebHookResource.class);
  }

  public class GlobalConfigurationLinks {

    public String self() {
      return linkBuilder
        .method("getConfiguration").parameters()
        .href();
    }

    public String update() {
      return linkBuilder
        .method("updateConfiguration").parameters()
        .href();
    }
  }

  public class RepositoryConfigurationLinks {

    public String self(String namespase, String name) {
      return linkBuilder
        .method("getRepositoryConfiguration").parameters(namespase, name)
        .href();
    }

    public String update(String namespase, String name) {
      return linkBuilder
        .method("updateRepositoryConfiguration").parameters(namespase, name)
        .href();
    }
  }
}
