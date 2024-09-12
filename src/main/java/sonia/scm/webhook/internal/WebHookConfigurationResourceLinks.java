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

    public String self(String namespace, String name) {
      return linkBuilder
        .method("getRepositoryConfiguration").parameters(namespace, name)
        .href();
    }

    public String update(String namespace, String name) {
      return linkBuilder
        .method("updateRepositoryConfiguration").parameters(namespace, name)
        .href();
    }
  }
}
