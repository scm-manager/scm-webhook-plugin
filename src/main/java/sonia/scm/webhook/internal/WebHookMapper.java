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
package sonia.scm.webhook.internal;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.otto.edison.hal.Links;
import org.mapstruct.AfterMapping;
import org.mapstruct.Context;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import sonia.scm.api.v2.resources.InstantAttributeMapper;
import sonia.scm.api.v2.resources.ScmPathInfoStore;
import sonia.scm.repository.Repository;
import sonia.scm.security.KeyGenerator;
import sonia.scm.webhook.AvailableWebHookSpecifications;
import sonia.scm.webhook.DtoAdapterWebHookSpecification;
import sonia.scm.webhook.SingleWebHookConfiguration;
import sonia.scm.webhook.WebHook;
import sonia.scm.webhook.WebHookConfiguration;
import sonia.scm.webhook.WebHookContext;

import javax.inject.Inject;
import java.util.Optional;

import static de.otto.edison.hal.Link.link;

@Mapper
public abstract class WebHookMapper implements InstantAttributeMapper {

  @Inject
  AvailableWebHookSpecifications availableSpecifications;

  @Inject
  ScmPathInfoStore scmPathInfoStore;

  @Inject
  ConfigurationValidator configurationValidator;

  @Inject
  KeyGenerator keyGenerator;

  public abstract WebHookConfigurationDto map(WebHookConfiguration configuration, @Context Repository repository);

  public abstract GlobalWebHookConfigurationDto map(WebHookConfiguration configuration);

  public abstract WebHookConfiguration map(WebHookConfigurationDto configurationDto);

  WebHookDto map(WebHook webHook) {
    WebHookDto dto = new WebHookDto();
    dto.setName(webHook.getName());
    dto.setId(webHook.getId());
    Optional<DtoAdapterWebHookSpecification> specification = availableSpecifications.specificationFor(webHook.getName());
    specification
      .map(spec -> spec.mapToDto(webHook.getConfiguration()))
      .ifPresent(dtoSpec -> dto.setConfiguration(new ObjectMapper().valueToTree(dtoSpec)));
    dto.setUnknown(!specification.isPresent());
    return dto;
  }

  WebHook map(WebHookDto dto) {
    WebHook webHook = new WebHook();
    webHook.setName(dto.getName());
    webHook.setId(dto.getId());
    availableSpecifications.specificationFor(dto.getName())
      .map(spec -> parseConfiguration(dto, spec))
      .ifPresent(spec -> webHook.setConfiguration(spec));
    return webHook;
  }

  private SingleWebHookConfiguration parseConfiguration(WebHookDto dto, DtoAdapterWebHookSpecification specification) {
    SingleWebHookConfiguration configuration;
    try {
      configuration = specification.mapFromDto(new ObjectMapper().treeToValue(dto.getConfiguration(), specification.getDtoType()));
      configurationValidator.validate(configuration);
      return configuration;
    } catch (JsonProcessingException e) {
      throw new InvalidConfigurationException(specification, e);
    }
  }

  @AfterMapping
  void addLinks(@MappingTarget WebHookConfigurationDto dto, @Context Repository repository) {
    WebHookConfigurationResourceLinks resourceLinks = new WebHookConfigurationResourceLinks(scmPathInfoStore.get());
    Links.Builder links = Links.linkingTo();
    links.self(resourceLinks.repositoryConfigurations.self(repository.getNamespace(), repository.getName()));
    if (WebHookContext.isWritePermitted(repository)) {
      links.single(link("update", resourceLinks.repositoryConfigurations.update(repository.getNamespace(), repository.getName())));
    }
    dto.add(links.build());
  }

  @AfterMapping
  void addLinks(@MappingTarget GlobalWebHookConfigurationDto dto) {
    WebHookConfigurationResourceLinks resourceLinks = new WebHookConfigurationResourceLinks(scmPathInfoStore.get());
    Links.Builder links = Links.linkingTo();
    links.self(resourceLinks.globalConfigurations.self());
    if (WebHookContext.isWritePermitted()) {
      links.single(link("update", resourceLinks.globalConfigurations.update()));
    }
    dto.add(links.build());
  }
}
