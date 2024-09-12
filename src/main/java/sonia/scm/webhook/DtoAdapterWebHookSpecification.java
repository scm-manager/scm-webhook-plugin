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

import sonia.scm.plugin.ExtensionPoint;
import sonia.scm.repository.PostReceiveRepositoryHookEvent;
import sonia.scm.repository.Repository;

@ExtensionPoint
public interface DtoAdapterWebHookSpecification<T extends SingleWebHookConfiguration, D> {

  Class<T> getSpecificationType();

  Class<D> getDtoType();

  default boolean handles(Class<WebHook> webHookClass) {
    return getSpecificationType().isAssignableFrom(webHookClass);
  }

  default boolean supportsRepository(Repository repository) {
    return true;
  }

  WebHookExecutor createExecutor(T webHook, Repository repository, PostReceiveRepositoryHookEvent event);

  D mapToDto(T configuration);

  T mapFromDto(D dto);

  default void updateBeforeStore(T oldConfiguration, T newConfiguration) {
  }
}
