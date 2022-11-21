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
