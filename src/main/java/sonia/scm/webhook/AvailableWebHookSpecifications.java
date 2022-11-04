package sonia.scm.webhook;

import com.google.inject.Inject;

import java.util.Set;

public class AvailableWebHookSpecifications {

  private final Set<WebHookSpecification<?>> specifications;

  @Inject
  public AvailableWebHookSpecifications(Set<WebHookSpecification<?>> specifications) {
    this.specifications = specifications;
  }

  public WebHookSpecification<?> specificationFor(String name) {
    return specifications.stream()
      .filter(specification -> specification.getSpecificationType().getSimpleName().equals(name))
      .findFirst()
      .orElseThrow(() -> new UnknownWebHookException(name));
  }

  public static String nameOf(WebHookSpecification<?> specification) {
    return nameOf(specification.getClass());
  }

  public static String nameOf(Class<? extends WebHookSpecification> specificationClass) {
    return specificationClass.getSimpleName();
  }
}
