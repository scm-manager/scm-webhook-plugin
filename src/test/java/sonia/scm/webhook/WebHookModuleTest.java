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

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sonia.scm.net.ahc.AdvancedHttpClient;
import sonia.scm.repository.RepositoryManager;
import sonia.scm.store.ConfigurationStoreFactory;
import sonia.scm.webhook.impl.AhcWebHookHttpClient;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class WebHookModuleTest {

  @Mock
  private AdvancedHttpClient ahClient;

  @Mock
  private RepositoryManager repositoryManager;

  @Mock
  private ConfigurationStoreFactory configurationStoreFactory;

  @Test
  void shouldBindAhcWebHookHttpClient() {
    Injector injector = Guice.createInjector(new WebHookModule(), new AbstractModule() {
      @Override
      protected void configure() {
        bind(AdvancedHttpClient.class).toInstance(ahClient);
        bind(RepositoryManager.class).toInstance(repositoryManager);
        bind(ConfigurationStoreFactory.class).toInstance(configurationStoreFactory);
      }
    });
    WebHookHttpClient client = injector.getInstance(WebHookHttpClient.class);
    assertThat(client).isInstanceOf(AhcWebHookHttpClient.class);
  }

}
