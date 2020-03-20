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

package sonia.scm.webhook.impl;

//~--- non-JDK imports --------------------------------------------------------

import com.google.inject.Singleton;

import org.codehaus.jackson.map.ObjectMapper;

import sonia.scm.webhook.WebHookMarshaller;

//~--- JDK imports ------------------------------------------------------------

import javax.ws.rs.core.MediaType;
import java.io.IOException;
import java.io.Writer;

/**
 *
 * @author Sebastian Sdorra
 */
@Singleton
public class JacksonWebHookMarshaller implements WebHookMarshaller
{

  /**
   * Method description
   *
   *
   * @param writer
   * @param data
   *
   * @throws IOException
   */
  @Override
  public void marshall(Writer writer, Object data) throws IOException
  {
    mapper.writeValue(writer, data);
  }

  @Override
  public MediaType getContentType() {
    return MediaType.APPLICATION_JSON_TYPE;
  }

  //~--- fields ---------------------------------------------------------------

  /** Field description */
  private final ObjectMapper mapper = new ObjectMapper();
}
