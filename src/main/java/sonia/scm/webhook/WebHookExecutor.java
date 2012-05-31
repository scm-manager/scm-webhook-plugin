/**
 * Copyright (c) 2010, Sebastian Sdorra All rights reserved.
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



package sonia.scm.webhook;

//~--- non-JDK imports --------------------------------------------------------

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sonia.scm.net.HttpClient;
import sonia.scm.net.HttpResponse;
import sonia.scm.repository.Changeset;
import sonia.scm.repository.Repository;

//~--- JDK imports ------------------------------------------------------------

import java.util.Collection;

/**
 *
 * @author Sebastian Sdorra
 */
public class WebHookExecutor implements Runnable
{

  /**
   * the logger for WebHookExecutor
   */
  private static final Logger logger =
    LoggerFactory.getLogger(WebHookExecutor.class);

  //~--- constructors ---------------------------------------------------------

  /**
   * Constructs ...
   *
   *
   *
   * @param httpClient
   * @param webHook
   * @param repository
   * @param changesets
   */
  public WebHookExecutor(HttpClient httpClient, WebHook webHook,
                         Repository repository,
                         Collection<Changeset> changesets)
  {
    this.httpClient = httpClient;
    this.webHook = webHook;
    this.repository = repository;
    this.changesets = changesets;
  }

  //~--- methods --------------------------------------------------------------

  /**
   * Method description
   *
   */
  @Override
  public void run()
  {
    if (webHook.isExecuteOnEveryCommit())
    {
      for (Changeset c : changesets)
      {
        String url = createUrl(webHook.getUrlPattern(), repository, c);

        if (webHook.isSendCommitData())
        {
          execute(url, c);
        }
      }
    }
    else
    {
      String url = createUrl(webHook.getUrlPattern(), repository, changesets);

      if (webHook.isSendCommitData())
      {
        execute(url, new Changesets(changesets));
      }
    }
  }

  /**
   * Method description
   *
   *
   * @param urlPattern
   * @param repository
   * @param changeset
   *
   * @return
   */
  private String createUrl(String urlPattern, Repository repository,
                           Changeset changeset)
  {

    // TODO implement
    return urlPattern;
  }

  /**
   * Method description
   *
   *
   * @param urlPattern
   * @param repository
   * @param changesets
   *
   * @return
   */
  private String createUrl(String urlPattern, Repository repository,
                           Collection<Changeset> changesets)
  {

    // TODO implement
    return urlPattern;
  }

  /**
   * Method description
   *
   *
   * @param url
   * @param data
   */
  private void execute(String url, Object data)
  {
    if (logger.isInfoEnabled())
    {
      logger.info("execute webhook for url {}", url);
    }

    // TODO implement data
    try
    {
      HttpResponse response = httpClient.get(url);
      int statusCode = response.getStatusCode();

      if ((statusCode >= 200) && (statusCode < 300))
      {
        if (logger.isInfoEnabled())
        {
          logger.info("webhook {} ended successfully with status code {}", url,
                      statusCode);
        }
      }
      else if (logger.isWarnEnabled())
      {
        logger.warn("webhook {} failed with statusCode {}", url, statusCode);
      }
    }
    catch (Exception ex)
    {
      logger.error("error during webhook execution for {}", url);
    }
  }

  //~--- fields ---------------------------------------------------------------

  /** Field description */
  private Collection<Changeset> changesets;

  /** Field description */
  private HttpClient httpClient;

  /** Field description */
  private Repository repository;

  /** Field description */
  private WebHook webHook;
}
