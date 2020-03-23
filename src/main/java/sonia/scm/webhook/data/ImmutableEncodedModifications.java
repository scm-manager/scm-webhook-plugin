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

import sonia.scm.repository.Modifications;

/**
 *
 * @author Sebastian Sdorra
 */
public final class ImmutableEncodedModifications
{

  /**
   * Constructs ...
   *
   *
   * @param modifications
   */
  public ImmutableEncodedModifications(Modifications modifications)
  {
    this.modifications = modifications;
  }

  //~--- methods --------------------------------------------------------------

  /**
   * Method description
   *
   *
   * @return
   */
  @Override
  public String toString()
  {
    StringBuilder out = new StringBuilder();

    out.append("A:").append(getAdded());
    out.append(";M:").append(getModified());
    out.append(";R:").append(getRemoved());

    return out.toString();
  }

  //~--- get methods ----------------------------------------------------------

  /**
   * Method description
   *
   *
   * @return
   */
  public EncodedStringList getAdded()
  {
    return new EncodedStringList(modifications.getAdded());
  }

  /**
   * Method description
   *
   *
   * @return
   */
  public EncodedStringList getModified()
  {
    return new EncodedStringList(modifications.getModified());
  }

  /**
   * Method description
   *
   *
   * @return
   */
  public EncodedStringList getRemoved()
  {
    return new EncodedStringList(modifications.getRemoved());
  }

  //~--- fields ---------------------------------------------------------------

  /** Field description */
  private final Modifications modifications;
}
