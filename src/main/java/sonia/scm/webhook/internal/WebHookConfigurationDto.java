package sonia.scm.webhook.internal;

import de.otto.edison.hal.HalRepresentation;
import de.otto.edison.hal.Links;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import java.util.HashSet;
import java.util.Set;

@AllArgsConstructor
@EqualsAndHashCode
@Getter
@Setter
public class WebHookConfigurationDto extends HalRepresentation {

  final Set<WebHookDto> webhooks = new HashSet<>();

  @Override
  @SuppressWarnings("squid:S1185") // We want to have this method available in this package
  protected HalRepresentation add(Links links) {
    return super.add(links);
  }
}
