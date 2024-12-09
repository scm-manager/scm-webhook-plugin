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

import jakarta.inject.Inject;
import sonia.scm.repository.Repository;
import sonia.scm.webhook.execution.WebHookExecution;

import java.util.List;

public final class WebHookService {
  private final WebHookContext context;
  private final WebHookSender sender;

  @Inject
  public WebHookService(WebHookContext context, WebHookSender sender) {
    this.context = context;
    this.sender = sender;
  }

  @SuppressWarnings("unchecked")
  public <T extends SingleWebHookConfiguration> List<T> getConfigurations(Class<T> configurationType, Repository repository) {
    WebHookConfiguration configurations = context.getAllConfigurations(repository);
    return (List<T>) configurations
      .getWebhooks()
      .stream()
      .filter(w -> w.isConfigurationFor(configurationType))
      .map(WebHook::getConfiguration)
      .toList();
  }

  public void execute(WebHookExecution execution) {
    sender.execute(execution);
  }
}
