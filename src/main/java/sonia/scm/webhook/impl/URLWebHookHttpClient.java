/**
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

package sonia.scm.webhook.impl;

//~--- non-JDK imports --------------------------------------------------------

import com.google.common.base.Strings;
import com.google.inject.Inject;
import org.apache.shiro.codec.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sonia.scm.SCMContextProvider;
import sonia.scm.config.ScmConfiguration;
import sonia.scm.net.Proxies;
import sonia.scm.util.Util;
import sonia.scm.webhook.HttpMethod;
import sonia.scm.webhook.WebHookHttpClient;
import sonia.scm.webhook.WebHookMarshaller;

import javax.ws.rs.core.MediaType;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.SocketAddress;
import java.net.URL;

//~--- JDK imports ------------------------------------------------------------

/**
 *
 * @author Sebastian Sdorra
 */
public class URLWebHookHttpClient implements WebHookHttpClient {

  /** Field description */
  private static final String CREDENTIAL_SEPARATOR = ":";

  /** Field description */
  private static final String HEADER_AUTHORIZATION = "Authorization";

  /** Field description */
  private static final String HEADER_CONTENT_TYPE = "Content-Type";

  /** Field description */
  private static final String HEADER_PROXY_AUTHORIZATION =
    "Proxy-Authorization";

  /** Field description */
  private static final String HEADER_USERAGENT = "User-Agent";

  /** Field description */
  private static final String HEADER_USERAGENT_VALUE =
    "SCM-Manager %s scm-webhook-plugin";

  /** Field description */
  private static final String PREFIX_BASIC_AUTHENTICATION = "Basic ";

  /** Field description */
  private static final int TIMEOUT_CONNECTION = 30000;

  /** Field description */
  private static final int TIMEOUT_RAED = 1200000;

  /**
   * the logger for URLWebHookHttpClient
   */
  private static final Logger logger =
    LoggerFactory.getLogger(URLWebHookHttpClient.class);

  //~--- constructors ---------------------------------------------------------

  /**
   * Constructs ...
   *
   *
   * @param context
   * @param configuration
   * @param marshaller
   */
  @Inject
  public URLWebHookHttpClient(SCMContextProvider context,
                              ScmConfiguration configuration, WebHookMarshaller marshaller) {
    this.context = context;
    this.configuration = configuration;
    this.marshaller = marshaller;
  }

  //~--- methods --------------------------------------------------------------

  /**
   * Method description
   *
   *
   * @param method
   * @param url
   *
   * @throws IOException
   */
  @Override
  public void execute(HttpMethod method, String url) throws IOException {
    execute(method, url, null);
  }

  /**
   * Method description
   *
   *
   * @param method
   * @param url
   * @param data
   *
   * @throws IOException
   */
  @Override
  public void execute(HttpMethod method, String url, Object data)
    throws IOException {
    HttpURLConnection connection = connect(url);

    HttpMethod m = method;

    if (m == HttpMethod.AUTO) {
      if (data != null) {
        m = HttpMethod.POST;
      } else {
        m = HttpMethod.GET;
      }
    }

    logger.debug("using http method {} for webhook request", m);
    connection.setRequestMethod(m.name());

    if (data != null) {
      connection.setDoOutput(true);
      MediaType contentType = marshaller.getContentType();
      if (contentType != null) {
        connection.setRequestProperty(HEADER_CONTENT_TYPE, contentType.toString());
      }


      try (OutputStreamWriter writer = new OutputStreamWriter(connection.getOutputStream())) {
        marshaller.marshall(writer, data);
      }
    }
    handleRespone(url, connection);
  }

  /**
   * Method description
   *
   *
   * @param connection
   * @param header
   * @param username
   * @param password
   */
  private void appendBasicAuthHeader(HttpURLConnection connection,
                                     String header, String username, String password) {
    if (Util.isNotEmpty(username) || Util.isNotEmpty(password)) {
      username = Util.nonNull(username);
      password = Util.nonNull(password);

      logger.debug("use authentication ({}) with user {}", header, username);

      String auth = username.concat(CREDENTIAL_SEPARATOR).concat(password);

      auth = new String(Base64.encode(auth.getBytes()));
      connection.addRequestProperty(header,
        PREFIX_BASIC_AUTHENTICATION.concat(auth));
    }
  }

  /**
   * Method description
   *
   *
   * @param urlString
   *
   * @return
   *
   * @throws IOException
   */
  private HttpURLConnection connect(String urlString) throws IOException {
    URL url = new URL(urlString);
    HttpURLConnection connection;

    if (Proxies.isEnabled(configuration, url)) {
      if (logger.isDebugEnabled()) {
        logger.debug("open url {} connection using proxy {}:{}",
          url.toExternalForm(), configuration.getProxyServer(),
          configuration.getProxyPort());
      }

      SocketAddress address =
        new InetSocketAddress(configuration.getProxyServer(),
          configuration.getProxyPort());

      connection =
        (HttpURLConnection) url.openConnection(new Proxy(Proxy.Type.HTTP,
          address));
    } else {
      connection = (HttpURLConnection) url.openConnection();
    }

    String userInfo = url.getUserInfo();

    if (!Strings.isNullOrEmpty(userInfo)) {
      String[] authParts = userInfo.split(CREDENTIAL_SEPARATOR);

      if (authParts.length == 2) {
        appendBasicAuthHeader(connection, HEADER_AUTHORIZATION, authParts[0],
          authParts[1]);
      } else {
        logger.warn("user info url part is malformed");
      }
    }

    //J-
    connection.setReadTimeout(TIMEOUT_RAED);
    connection.setConnectTimeout(TIMEOUT_CONNECTION);
    connection.setRequestProperty(HEADER_USERAGENT,
      String.format(HEADER_USERAGENT_VALUE, context.getVersion())
    );
    //J+

    appendBasicAuthHeader(connection, HEADER_PROXY_AUTHORIZATION,
      configuration.getProxyUser(), configuration.getProxyPassword());

    return connection;
  }

  /**
   * Method description
   *
   *
   * @param url
   * @param connection
   *
   * @throws IOException
   */
  private void handleRespone(String url, HttpURLConnection connection)
    throws IOException {
    int statusCode = connection.getResponseCode();

    if ((statusCode >= 200) && (statusCode < 300)) {
      logger.info("webhook {} ended successfully with status code {}", url,
        statusCode);
    } else if (logger.isWarnEnabled()) {
      logger.warn("webhook {} failed with statusCode {}", url, statusCode);
    }
  }

  //~--- fields ---------------------------------------------------------------

  /** Field description */
  private final ScmConfiguration configuration;

  /** Field description */
  private final SCMContextProvider context;

  /** Field description */
  private final WebHookMarshaller marshaller;
}
