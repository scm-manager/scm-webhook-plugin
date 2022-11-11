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
package sonia.scm.webhook.internal;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.shiro.subject.Subject;
import org.apache.shiro.subject.support.SubjectThreadState;
import org.apache.shiro.util.ThreadContext;
import org.apache.shiro.util.ThreadState;
import org.jboss.resteasy.mock.MockHttpRequest;
import org.jboss.resteasy.mock.MockHttpResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import sonia.scm.api.v2.resources.ScmPathInfoStore;
import sonia.scm.repository.NamespaceAndName;
import sonia.scm.repository.Repository;
import sonia.scm.repository.RepositoryManager;
import sonia.scm.web.RestDispatcher;
import sonia.scm.webhook.AvailableWebHookSpecifications;
import sonia.scm.webhook.HttpMethod;
import sonia.scm.webhook.SimpleWebHook;
import sonia.scm.webhook.SimpleWebHookSpecification;
import sonia.scm.webhook.WebHook;
import sonia.scm.webhook.WebHookConfiguration;
import sonia.scm.webhook.WebHookContext;

import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.core.MediaType;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import static java.util.Collections.singleton;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class WebHookResourceTest {

  private final Subject subject = mock(Subject.class);
  private final ThreadState subjectThreadState = new SubjectThreadState(subject);

  @Mock
  WebHookContext context;
  @Mock
  RepositoryManager repositoryManager;

  WebHookMapper webHookMapper = new WebHookMapperImpl();

  WebHookResource resource;
  private RestDispatcher dispatcher;
  private final MockHttpResponse response = new MockHttpResponse();
  public static final String WEB_HOOKS = "{\n" +
    "    \"webhooks\": [\n" +
    "        {\n\"name\": \"SimpleWebHook\", \"configuration\": {" +
    "            \"urlPattern\": \"url/{repository.id}/pattern\",\n" +
    "            \"executeOnEveryCommit\": true,\n" +
    "            \"sendCommitData\": true,\n" +
    "            \"method\": \"GET\"\n" +
    "        }},\n" +
    "        {\n\"name\": \"SimpleWebHook\", \"configuration\": {" +
    "            \"urlPattern\": \"url2/{repository.id}/pattern\",\n" +
    "            \"executeOnEveryCommit\": false,\n" +
    "            \"sendCommitData\": false,\n" +
    "            \"method\": \"POST\"\n" +
    "        }}\n" +
    "    ]\n" +
    "}";

  @BeforeEach
  void init() {
    subjectThreadState.bind();
    ThreadContext.bind(subject);
    resource = new WebHookResource(context, webHookMapper, repositoryManager);
    when(repositoryManager.get(new NamespaceAndName("space", "name"))).thenReturn(new Repository("id", "git", "space", "name"));
    dispatcher = new RestDispatcher();
    dispatcher.addSingletonResource(resource);

    ScmPathInfoStore scmPathInfoStore = new ScmPathInfoStore();
    scmPathInfoStore.set(() -> URI.create("/"));
    webHookMapper.scmPathInfoStore = scmPathInfoStore;
    webHookMapper.availableSpecifications = new AvailableWebHookSpecifications(singleton(new SimpleWebHookSpecification(null, null)));
    webHookMapper.configurationValidator = new ConfigurationValidator();
  }

  @Test
  void shouldGetWebHookConfigurations() throws URISyntaxException, IOException {
    WebHookConfiguration configs = new WebHookConfiguration();
    configs.getWebhooks().add(new WebHook(new SimpleWebHook("/url_1/pattern/{repository.id}", true, false, HttpMethod.GET)));
    configs.getWebhooks().add(new WebHook(new SimpleWebHook("/url_2/pattern/{repository.id}", false, false, HttpMethod.POST)));

    when(context.getGlobalConfiguration()).thenReturn(configs);
    MockHttpRequest request = MockHttpRequest
      .get("/" + WebHookResource.PATH)
      .accept(MediaType.APPLICATION_JSON);

    System.out.println(response.getContentAsString());

    dispatcher.invoke(request, response);
    assertThat(response.getStatus()).isEqualTo(HttpServletResponse.SC_OK);
    ObjectMapper mapper = new ObjectMapper();
    JsonNode jsonNode = mapper.readValue(response.getContentAsString(), JsonNode.class);
    JsonNode webhookNode = jsonNode.get("webhooks");
    JsonNode webHook_1 = webhookNode.path(0);
    JsonNode webHook_2 = webhookNode.path(1);
    assertThat(webHook_1.get("configuration").get("urlPattern").asText()).isIn("/url_1/pattern/{repository.id}", "/url_2/pattern/{repository.id}");
    assertThat(webHook_2.get("configuration").get("urlPattern").asText()).isIn("/url_1/pattern/{repository.id}", "/url_2/pattern/{repository.id}");

    verify(context).getGlobalConfiguration();
  }

  @Test
  void shouldPostWebHookConfigurations() throws URISyntaxException {

    MockHttpRequest request = MockHttpRequest
      .post("/" + WebHookResource.PATH)
      .contentType(MediaType.APPLICATION_JSON)
      .content(WEB_HOOKS.getBytes());

    dispatcher.invoke(request, response);
    assertThat(response.getStatus()).isEqualTo(HttpServletResponse.SC_NO_CONTENT);
    verify(context).setGlobalConfiguration(argThat(webHookConfiguration -> {
      assertThat(webHookConfiguration.getWebhooks()).hasSize(2);
      assertThat(webHookConfiguration.getWebhooks()).extracting("configuration").containsExactlyInAnyOrder(
        new SimpleWebHook("url/{repository.id}/pattern", true, true, HttpMethod.GET),
        new SimpleWebHook("url2/{repository.id}/pattern", false, false, HttpMethod.POST));
      return true;
    }));
  }

  @Test
  void shouldUpdateWebHookConfigurations() throws URISyntaxException {
    MockHttpRequest request = MockHttpRequest
      .put("/" + WebHookResource.PATH)
      .contentType(MediaType.APPLICATION_JSON)
      .content(WEB_HOOKS.getBytes());

    dispatcher.invoke(request, response);
    assertThat(response.getStatus()).isEqualTo(HttpServletResponse.SC_NO_CONTENT);
    verify(context).setGlobalConfiguration(argThat(webHookConfiguration -> {
      assertThat(webHookConfiguration.getWebhooks()).hasSize(2);
      assertThat(webHookConfiguration.getWebhooks()).extracting("configuration").containsExactlyInAnyOrder(
        new SimpleWebHook("url/{repository.id}/pattern", true, true, HttpMethod.GET),
        new SimpleWebHook("url2/{repository.id}/pattern", false, false, HttpMethod.POST));
      return true;
    }));
  }


  @Test
  void shouldGetRepoWebHookConfigurations() throws URISyntaxException, IOException {
    WebHookConfiguration configs = new WebHookConfiguration();
    configs.getWebhooks().add(new WebHook(new SimpleWebHook("/url_1/pattern/{repository.id}", true, false, HttpMethod.GET)));
    configs.getWebhooks().add(new WebHook(new SimpleWebHook("/url_2/pattern/{repository.id}", false, false, HttpMethod.POST)));

    when(context.getRepositoryConfigurations("space", "name")).thenReturn(configs);
    MockHttpRequest request = MockHttpRequest
      .get("/" + WebHookResource.PATH + "/space/name")
      .accept(MediaType.APPLICATION_JSON);

    dispatcher.invoke(request, response);
    assertThat(response.getStatus()).isEqualTo(HttpServletResponse.SC_OK);
    ObjectMapper mapper = new ObjectMapper();
    JsonNode jsonNode = mapper.readValue(response.getContentAsString(), JsonNode.class);
    JsonNode prNode = jsonNode.get("webhooks");
    JsonNode webHook_1 = prNode.path(0);
    JsonNode webHook_2 = prNode.path(1);
    assertThat(webHook_1.get("configuration").get("urlPattern").asText()).isIn("/url_1/pattern/{repository.id}", "/url_2/pattern/{repository.id}");
    assertThat(webHook_2.get("configuration").get("urlPattern").asText()).isIn("/url_1/pattern/{repository.id}", "/url_2/pattern/{repository.id}");

    verify(context).getRepositoryConfigurations("space", "name");
  }

  @Test
  void shouldPostRepoWebHookConfigurations() throws URISyntaxException {

    MockHttpRequest request = MockHttpRequest
      .post("/" + WebHookResource.PATH + "/space/name")
      .contentType(MediaType.APPLICATION_JSON)
      .content(WEB_HOOKS.getBytes());

    dispatcher.invoke(request, response);
    assertThat(response.getStatus()).isEqualTo(HttpServletResponse.SC_NO_CONTENT);
    verify(context).setRepositoryConfiguration(argThat(webHookConfiguration -> {
      assertThat(webHookConfiguration.getWebhooks()).hasSize(2);
      assertThat(webHookConfiguration.getWebhooks()).extracting("configuration").containsExactlyInAnyOrder(
        new SimpleWebHook("url/{repository.id}/pattern", true, true, HttpMethod.GET),
        new SimpleWebHook("url2/{repository.id}/pattern", false, false, HttpMethod.POST));
      return true;
    }), eq("space"), eq("name"));
  }

 @Test
  void shouldUpdateRepoWebHookConfigurations() throws URISyntaxException {

    MockHttpRequest request = MockHttpRequest
      .put("/" + WebHookResource.PATH + "/space/name")
      .contentType(MediaType.APPLICATION_JSON)
      .content(WEB_HOOKS.getBytes());

    dispatcher.invoke(request, response);
    assertThat(response.getStatus()).isEqualTo(HttpServletResponse.SC_NO_CONTENT);
    verify(context).setRepositoryConfiguration(argThat(webHookConfiguration -> {
      assertThat(webHookConfiguration.getWebhooks()).hasSize(2);
      assertThat(webHookConfiguration.getWebhooks()).extracting("configuration").containsExactlyInAnyOrder(
        new SimpleWebHook("url/{repository.id}/pattern", true, true, HttpMethod.GET),
        new SimpleWebHook("url2/{repository.id}/pattern", false, false, HttpMethod.POST));
      return true;
    }), eq("space"), eq("name"));
  }

}
