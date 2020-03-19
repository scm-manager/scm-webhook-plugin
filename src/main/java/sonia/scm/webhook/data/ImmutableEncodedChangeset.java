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


package sonia.scm.webhook.data;

//~--- non-JDK imports --------------------------------------------------------

import sonia.scm.repository.Changeset;

/**
 *
 * @author Sebastian Sdorra
 */
public final class ImmutableEncodedChangeset
{

  /**
   * Constructs ...
   *
   *
   * @param changeset
   */
  public ImmutableEncodedChangeset(Changeset changeset)
  {
    this.changeset = changeset;
  }

  //~--- get methods ----------------------------------------------------------

  /**
   * Method description
   *
   *
   * @return
   */
  public ImmutableEncodedPerson getAuthor()
  {
    return new ImmutableEncodedPerson(changeset.getAuthor());
  }

  /**
   * Method description
   *
   *
   * @return
   */
  public EncodedStringList getBranches()
  {
    return new EncodedStringList(changeset.getBranches());
  }

  /**
   * Method description
   *
   *
   * @return
   */
  public Long getDate()
  {
    return changeset.getDate();
  }

  /**
   * Method description
   *
   *
   * @return
   */
  public String getDescription()
  {
    return Encoder.encode(changeset.getDescription());
  }

  /**
   * Method description
   *
   *
   * @return
   */
  public String getId()
  {
    return changeset.getId();
  }

  /**
   * Method description
   *
   *
   * @return
   */
  public EncodedStringList getParents()
  {
    return new EncodedStringList(changeset.getParents());
  }

  /**
   * Method description
   *
   *
   * @return
   */
  public EncodedStringList getTags()
  {
    return new EncodedStringList(changeset.getTags());
  }

  //~--- fields ---------------------------------------------------------------

  /** Field description */
  private final Changeset changeset;
}
