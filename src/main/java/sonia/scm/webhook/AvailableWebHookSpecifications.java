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

import com.google.inject.Inject;
import sonia.scm.repository.Repository;

import java.util.Collection;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public class AvailableWebHookSpecifications {

  private final Set<DtoAdapterWebHookSpecification> specifications;

  @Inject
  public AvailableWebHookSpecifications(Set<DtoAdapterWebHookSpecification> specifications) {
    this.specifications = specifications;
  }

  public Optional<DtoAdapterWebHookSpecification> specificationFor(String name) {
    return specifications.stream()
      .filter(specification -> specification.getSpecificationType().getSimpleName().equals(name))
      .findFirst();
  }

  public Collection<String> getTypesFor(Repository repository) {
    return specifications.stream()
      .filter(specification -> specification.supportsRepository(repository))
      .map(DtoAdapterWebHookSpecification::getSpecificationType)
      .map(Class::getSimpleName)
      .collect(Collectors.toList());
  }

  public static String nameOf(DtoAdapterWebHookSpecification specification) {
    return nameOf(specification.getClass());
  }

  public static String nameOf(Class<? extends DtoAdapterWebHookSpecification> specificationClass) {
    return specificationClass.getSimpleName();
  }
}
