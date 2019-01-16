package sonia.scm.webhook.internal;

import de.otto.edison.hal.HalRepresentation;
import de.otto.edison.hal.Links;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import sonia.scm.webhook.HttpMethod;

@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
@Getter
@Setter
public class WebHookDto extends HalRepresentation {
  private String urlPattern;
  private boolean executeOnEveryCommit;
  private boolean sendCommitData;
  private HttpMethod method = HttpMethod.AUTO;

  @Override
  @SuppressWarnings("squid:S1185") // We want to have this method available in this package
  protected HalRepresentation add(Links links) {
    return super.add(links);
  }
}
