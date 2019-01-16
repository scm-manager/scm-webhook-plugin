package sonia.scm.webhook.internal;

import sonia.scm.api.v2.resources.LinkBuilder;
import sonia.scm.api.v2.resources.ScmPathInfo;

public class WebHookConfigurationResourceLinks {
  private final LinkBuilder linkBuilder;

  public WebHookConfigurationResourceLinks(ScmPathInfo scmPathInfo) {
    this.linkBuilder = new LinkBuilder(scmPathInfo, WebHookResource.class);
  }

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
