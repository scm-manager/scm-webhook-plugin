/**
 * Copyright (c) 2014, Sebastian Sdorra All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice,
 * this list of conditions and the following disclaimer. 2. Redistributions in
 * binary form must reproduce the above copyright notice, this list of
 * conditions and the following disclaimer in the documentation and/or other
 * materials provided with the distribution. 3. Neither the name of SCM-Manager;
 * nor the names of its contributors may be used to endorse or promote products
 * derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE REGENTS OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * http://bitbucket.org/sdorra/scm-manager
 *
 */



package sonia.scm.webhook.impl;

//~--- non-JDK imports --------------------------------------------------------

import com.google.common.io.Closeables;
import com.google.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sonia.scm.SCMContextProvider;
import sonia.scm.config.ScmConfiguration;
import sonia.scm.net.Proxies;
import sonia.scm.util.Util;
import sonia.scm.webhook.WebHookHttpClient;
import sonia.scm.webhook.WebHookMarshaller;

//~--- JDK imports ------------------------------------------------------------

import com.sun.jersey.core.util.Base64;

import java.io.IOException;
import java.io.OutputStreamWriter;

import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.SocketAddress;
import java.net.URL;

/**
 *
 * @author Sebastian Sdorra
 */
public class URLWebHookHttpClient implements WebHookHttpClient
{

  /** Field description */
  private static final String CREDENTIAL_SEPARATOR = ":";

  /** Field description */
  private static final String HEADER_PROXY_AUTHORIZATION =
    "Proxy-Authorization";

  /** Field description */
  private static final String HEADER_USERAGENT = "User-Agent";

  /** Field description */
  private static final String HEADER_USERAGENT_VALUE =
    "SCM-Manager %s scm-webhook-plugin";

  /** Field description */
  private static final String METHOD_POST = "POST";

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
    ScmConfiguration configuration, WebHookMarshaller marshaller)
  {
    this.context = context;
    this.configuration = configuration;
    this.marshaller = marshaller;
  }

  //~--- methods --------------------------------------------------------------

  /**
   * Method description
   *
   *
   * @param url
   * @param data
   *
   * @throws IOException
   */
  @Override
  public void post(String url, Object data) throws IOException
  {
    HttpURLConnection connection = connect(url);

    connection.setRequestMethod(METHOD_POST);
    connection.setDoOutput(true);

    OutputStreamWriter writer;

    try
    {
      writer = new OutputStreamWriter(connection.getOutputStream());
      marshaller.marshall(writer, data);
    }
    finally
    {
      Closeables.close(context, true);
    }
    
    handleRespone(url, connection);
  }

  //~--- get methods ----------------------------------------------------------

  /**
   * Method description
   *
   *
   * @param url
   *
   * @throws IOException
   */
  @Override
  public void get(String url) throws IOException
  {
    handleRespone(url, connect(url));
  }

  //~--- methods --------------------------------------------------------------

  /**
   * Method description
   *
   *
   * @param connection
   * @param username
   * @param password
   */
  private void appendProxyAuthHeader(HttpURLConnection connection,
    String username, String password)
  {
    if (Util.isNotEmpty(username) || Util.isNotEmpty(password))
    {
      username = Util.nonNull(username);
      password = Util.nonNull(password);

      logger.debug("use proxy authentication with user {}", username);

      String auth = username.concat(CREDENTIAL_SEPARATOR).concat(password);

      auth = new String(Base64.encode(auth.getBytes()));
      connection.addRequestProperty(HEADER_PROXY_AUTHORIZATION,
        PREFIX_BASIC_AUTHENTICATION.concat(auth));
    }
  }

  /**
   * Method description
   *
   *
   * @param url
   *
   * @param urlString
   *
   * @return
   *
   * @throws IOException
   */
  private HttpURLConnection connect(String urlString) throws IOException
  {
    URL url = new URL(urlString);
    HttpURLConnection connection;

    if (Proxies.isEnabled(configuration, url))
    {
      if (logger.isDebugEnabled())
      {
        logger.debug("open url connection using proxy {}:{}",
          url.toExternalForm(), configuration.getProxyServer(),
          configuration.getProxyPort());
      }

      SocketAddress address =
        new InetSocketAddress(configuration.getProxyServer(),
          configuration.getProxyPort());

      connection =
        (HttpURLConnection) url.openConnection(new Proxy(Proxy.Type.HTTP,
          address));
    }
    else
    {
      connection = (HttpURLConnection) url.openConnection();
    }

    //J-
    connection.setReadTimeout(TIMEOUT_RAED);
    connection.setConnectTimeout(TIMEOUT_CONNECTION);
    connection.setRequestProperty(HEADER_USERAGENT, 
      String.format(HEADER_USERAGENT_VALUE, context.getVersion())
    );
    //J+

    appendProxyAuthHeader(connection, configuration.getProxyUser(),
      configuration.getProxyPassword());

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
    throws IOException
  {
    int statusCode = connection.getResponseCode();

    if ((statusCode >= 200) && (statusCode < 300))
    {
      logger.info("webhook {} ended successfully with status code {}", url,
        statusCode);
    }
    else if (logger.isWarnEnabled())
    {
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
