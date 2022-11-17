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

package sonia.scm.webhook;

import com.google.inject.Inject;
import sonia.scm.repository.Repository;

import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;

public class AvailableWebHookSpecifications {

  private final Set<DtoAdapterWebHookSpecification> specifications;

  @Inject
  public AvailableWebHookSpecifications(Set<DtoAdapterWebHookSpecification> specifications) {
    this.specifications = specifications;
  }

  public DtoAdapterWebHookSpecification specificationFor(String name) {
    return specifications.stream()
      .filter(specification -> specification.getSpecificationType().getSimpleName().equals(name))
      .findFirst()
      .orElseThrow(() -> new UnknownWebHookException(name));
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
