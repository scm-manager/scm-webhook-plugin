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

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * @author Sebastian Sdorra
 */
@XmlRootElement(name = "webhooks")
@XmlAccessorType(XmlAccessType.FIELD)
@EqualsAndHashCode
@Getter
@NoArgsConstructor
public class WebHookConfiguration {

  @XmlElement(name = "webhook")
  private final List<WebHook> webhooks = new ArrayList<>();

  public WebHookConfiguration(Collection<WebHook> webhooks) {
    this.webhooks.addAll(webhooks);
  }

  public WebHookConfiguration merge(WebHookConfiguration otherConfiguration) {
    Collection<WebHook> allHooks = new ArrayList<>();

    allHooks.addAll(webhooks);
    allHooks.addAll(otherConfiguration.webhooks);

    return new WebHookConfiguration(allHooks);
  }

  public boolean isWebHookAvailable() {
    return !webhooks.isEmpty();
  }

}
