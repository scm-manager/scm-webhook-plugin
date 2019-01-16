package sonia.scm.webhook.internal;

import de.otto.edison.hal.Links;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import sonia.scm.webhook.WebHook;
import sonia.scm.webhook.WebHookConfiguration;
import sonia.scm.webhook.WebHookContext;

import javax.ws.rs.core.UriInfo;
import java.net.URI;

import static de.otto.edison.hal.Link.link;

@Mapper
public abstract class WebHookMapper {

  private WebHookConfigurationResourceLinks resourceLinks = new WebHookConfigurationResourceLinks(() -> URI.create("/"));

  @Mapping(target = "attributes", ignore = true)
  protected abstract WebHookConfigurationDto map(WebHookConfiguration webHookConfiguration);

  protected abstract WebHookConfiguration map(WebHookConfigurationDto dto);

  @Mapping(target = "attributes", ignore = true)
  protected abstract WebHookDto map(WebHook webHook);

  protected abstract WebHook map(WebHookDto dto);

  public WebHookMapper using(UriInfo uriInfo) {
    resourceLinks = new WebHookConfigurationResourceLinks(uriInfo::getBaseUri);
    return this;
  }

  @AfterMapping
  void addLinks(@MappingTarget WebHookConfigurationDto dto) {
    Links.Builder links = Links.linkingTo();
    links.self(resourceLinks.self());
    if ( WebHookContext.isWritePermitted()) {
      links.single(link("update", resourceLinks.update()));
    }
    dto.add(links.build());
  }

}
