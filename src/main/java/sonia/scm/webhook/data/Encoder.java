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
package sonia.scm.webhook.data;

//~--- non-JDK imports --------------------------------------------------------

import com.google.common.collect.Lists;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.List;

//~--- JDK imports ------------------------------------------------------------

/**
 * @author Sebastian Sdorra
 */
public final class Encoder {

  /**
   * Field description
   */
  private static final String ENCODING = "UTF-8";

  /**
   * the logger for Encoder
   */
  private static final Logger logger = LoggerFactory.getLogger(Encoder.class);

  //~--- constructors ---------------------------------------------------------

  /**
   * Constructs ...
   */
  private Encoder() {
  }

  //~--- methods --------------------------------------------------------------

  /**
   * Method description
   *
   * @param value
   * @return
   */
  public static String encode(String value) {
    if (value != null) {
      try {
        value = URLEncoder.encode(value, ENCODING);
      } catch (UnsupportedEncodingException ex) {
        logger.error("enocding is not supported", ex);
      }

    }

    return value;
  }

  /**
   * Method description
   *
   * @param values
   * @return
   */
  public static List<String> encode(List<String> values) {
    return Lists.transform(values, Encoder::encode);
  }
}
