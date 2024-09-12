/*
 * Copyright (c) 2020 - present Cloudogu GmbH
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU Affero General Public License as published by the Free
 * Software Foundation, version 3.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see https://www.gnu.org/licenses/.
 */

package sonia.scm.webhook;

//~--- non-JDK imports --------------------------------------------------------

import sonia.scm.repository.Changeset;

//~--- JDK imports ------------------------------------------------------------


import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;

/**
 *
 * @author Sebastian Sdorra
 */
@XmlRootElement(name = "changesets")
@XmlAccessorType(XmlAccessType.FIELD)
public class Changesets
{

  /**
   * Constructs ...
   *
   */
  public Changesets() {}

  /**
   * Constructs ...
   *
   *
   * @param changesets
   */
  public Changesets(Iterable<Changeset> changesets)
  {
    this.changesetList = changesets;
  }

  //~--- get methods ----------------------------------------------------------

  /**
   * Method description
   *
   *
   * @return
   */
  public Iterable<Changeset> getChangesets()
  {
    return changesetList;
  }

  //~--- fields ---------------------------------------------------------------

  /** Field description */
  @XmlElement(name = "changeset")
  private Iterable<Changeset> changesetList;
}
