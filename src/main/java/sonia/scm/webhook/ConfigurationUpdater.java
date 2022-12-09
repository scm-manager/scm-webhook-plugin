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

import org.apache.commons.lang.StringUtils;
import sonia.scm.security.KeyGenerator;

import javax.inject.Inject;
import java.util.Objects;
import java.util.Optional;

class ConfigurationUpdater {

  private final AvailableWebHookSpecifications specifications;
  private final KeyGenerator keyGenerator;

  @Inject
  ConfigurationUpdater(AvailableWebHookSpecifications specifications, KeyGenerator keyGenerator) {
    this.specifications = specifications;
    this.keyGenerator = keyGenerator;
  }

  WebHookConfiguration update(WebHookConfiguration oldConfiguration, WebHookConfiguration newConfiguration) {
    newConfiguration.getWebhooks()
      .forEach(webHook -> {
        if (StringUtils.isEmpty(webHook.getId())) {
          webHook.setId(keyGenerator.createKey());
        }
        Optional<? extends SingleWebHookConfiguration> oldSingleConfiguration = find(oldConfiguration, webHook.getId());
        specifications.specificationFor(webHook.getName())
          .ifPresent(spec ->
            oldSingleConfiguration.ifPresent(configuration -> spec.updateBeforeStore(configuration, webHook.getConfiguration())));
      });
    return newConfiguration;
  }

  private Optional<SingleWebHookConfiguration> find(WebHookConfiguration oldConfiguration, String id) {
    if (oldConfiguration == null || oldConfiguration.getWebhooks() == null) {
      return Optional.empty();
    }
    return oldConfiguration.getWebhooks()
      .stream()
      .filter(webHook -> id.equals(webHook.getId()))
      .map(WebHook::getConfiguration)
      .filter(Objects::nonNull)
      .findFirst();
  }
}
