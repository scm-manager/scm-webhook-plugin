package sonia.scm.webhook.internal;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.shiro.subject.Subject;
import org.apache.shiro.subject.support.SubjectThreadState;
import org.apache.shiro.util.ThreadContext;
import org.apache.shiro.util.ThreadState;
import org.jboss.resteasy.core.Dispatcher;
import org.jboss.resteasy.mock.MockDispatcherFactory;
import org.jboss.resteasy.mock.MockHttpRequest;
import org.jboss.resteasy.mock.MockHttpResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import sonia.scm.webhook.HttpMethod;
import sonia.scm.webhook.WebHook;
import sonia.scm.webhook.WebHookConfiguration;
import sonia.scm.webhook.WebHookContext;

import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.core.MediaType;
import java.io.IOException;
import java.net.URISyntaxException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.argThat;
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

  WebHookMapper webHookMapper = new WebHookMapperImpl();

  WebHookResource resource;
  private Dispatcher dispatcher;
  private final MockHttpResponse response = new MockHttpResponse();

  @BeforeEach
  void init() {
    subjectThreadState.bind();
    ThreadContext.bind(subject);
    resource = new WebHookResource(context, webHookMapper);
    dispatcher = MockDispatcherFactory.createDispatcher();
    dispatcher.getRegistry().addSingletonResource(resource);
  }


  @Test
  void shouldGetWebHookConfigurations() throws URISyntaxException, IOException {
    WebHookConfiguration configs = new WebHookConfiguration();
    configs.getWebhooks().add(new WebHook("/url_1/pattern/{}", true, false, HttpMethod.GET));
    configs.getWebhooks().add(new WebHook("/url_2/pattern/{}", false, false, HttpMethod.POST));

    when(context.getGlobalConfiguration()).thenReturn(configs);
    MockHttpRequest request = MockHttpRequest
      .get("/" + WebHookResource.PATH)
      .accept(MediaType.APPLICATION_JSON);

    dispatcher.invoke(request, response);
    assertEquals(HttpServletResponse.SC_OK, response.getStatus());
    ObjectMapper mapper = new ObjectMapper();
    JsonNode jsonNode = mapper.readValue(response.getContentAsString(), JsonNode.class);
    JsonNode prNode = jsonNode.get("webhooks");
    JsonNode webHook_1 = prNode.path(0);
    JsonNode webHook_2 = prNode.path(1);
    assertThat(webHook_1.get("urlPattern").asText()).isIn("/url_1/pattern/{}", "/url_2/pattern/{}");
    assertThat(webHook_2.get("urlPattern").asText()).isIn("/url_1/pattern/{}", "/url_2/pattern/{}");

    verify(context).getGlobalConfiguration();
  }

  @Test
  void shouldPostWebHookConfigurations() throws URISyntaxException, IOException {

    String webHooks = "{\n" +
      "    \"webhooks\": [\n" +
      "        {\n" +
      "            \"urlPattern\": \"url/{}/pattern\",\n" +
      "            \"executeOnEveryCommit\": true,\n" +
      "            \"sendCommitData\": true,\n" +
      "            \"method\": \"GET\"\n" +
      "        },\n" +
      "        {\n" +
      "            \"urlPattern\": \"url2/{}/pattern\",\n" +
      "            \"executeOnEveryCommit\": false,\n" +
      "            \"sendCommitData\": false,\n" +
      "            \"method\": \"POST\"\n" +
      "        }\n" +
      "    ]\n" +
      "}";
    MockHttpRequest request = MockHttpRequest
      .post("/" + WebHookResource.PATH)
      .contentType(MediaType.APPLICATION_JSON)
      .content(webHooks.getBytes());

    dispatcher.invoke(request, response);
    assertEquals(HttpServletResponse.SC_NO_CONTENT, response.getStatus());
    verify(context).setGlobalConfiguration(argThat(webHookConfiguration -> {
      assertThat(webHookConfiguration.getWebhooks()).hasSize(2);
      assertThat(webHookConfiguration.getWebhooks()).containsExactlyInAnyOrder(
        new WebHook("url/{}/pattern", true, true, HttpMethod.GET),
        new WebHook("url2/{}/pattern", false, false, HttpMethod.POST));
      return true;
    }));
  }

}
