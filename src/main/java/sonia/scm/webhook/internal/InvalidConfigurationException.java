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

package sonia.scm.webhook.internal;

import sonia.scm.BadRequestException;
import sonia.scm.ContextEntry;
import sonia.scm.webhook.AvailableWebHookSpecifications;
import sonia.scm.webhook.DtoAdapterWebHookSpecification;
import sonia.scm.webhook.WebHookSpecification;

@SuppressWarnings("squid:MaximumInheritanceDepth") // exceptions have a deep inheritance depth themselves; therefore we accept this here
public class InvalidConfigurationException extends BadRequestException {

  public static final String CODE = "BcTMDIHIb1";

  public InvalidConfigurationException(DtoAdapterWebHookSpecification specification, Exception cause) {
    super(ContextEntry.ContextBuilder.entity("webhook", AvailableWebHookSpecifications.nameOf(specification)).build(), "configuration could not be parsed", cause);
  }

  @Override
  public String getCode() {
    return CODE;
  }
}
