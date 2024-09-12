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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.otto.edison.hal.Links;
import org.mapstruct.AfterMapping;
import org.mapstruct.Context;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
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

import jakarta.inject.Inject;
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

  @Mapping(ignore = true, target = "attributes")
  public abstract WebHookConfigurationDto map(WebHookConfiguration configuration, @Context Repository repository);

  @Mapping(ignore = true, target = "attributes")
  public abstract GlobalWebHookConfigurationDto map(WebHookConfiguration configuration);

  @Mapping(ignore = true, target = "merge")
  public abstract WebHookConfiguration map(WebHookConfigurationDto configurationDto);

  WebHookDto map(WebHook webHook) {
    WebHookDto dto = new WebHookDto();
    dto.setName(webHook.getName());
    dto.setId(webHook.getId());
    Optional<DtoAdapterWebHookSpecification> specification = availableSpecifications.specificationFor(webHook.getName());
    specification
      .map(spec -> spec.mapToDto(webHook.getConfiguration()))
      .ifPresent(dtoSpec -> dto.setConfiguration(new ObjectMapper().valueToTree(dtoSpec)));
    dto.setUnknown(specification.isEmpty());
    return dto;
  }

  WebHook map(WebHookDto dto) {
    WebHook webHook = new WebHook();
    webHook.setName(dto.getName());
    webHook.setId(dto.getId());
    availableSpecifications.specificationFor(dto.getName())
      .map(spec -> parseConfiguration(dto, spec))
      .ifPresent(webHook::setConfiguration);
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
