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

import com.google.inject.Inject;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import sonia.scm.api.v2.resources.ErrorDto;
import sonia.scm.repository.NamespaceAndName;
import sonia.scm.repository.Repository;
import sonia.scm.repository.RepositoryManager;
import sonia.scm.web.VndMediaType;
import sonia.scm.webhook.WebHookContext;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

/**
 * @author Sebastian Sdorra
 */
@OpenAPIDefinition(tags = {
  @Tag(name = "Webhook Plugin", description = "Webhook plugin provided endpoints")
})
@Path(WebHookResource.PATH)
public class WebHookResource {

  public static final String PATH = "v2/plugins/webhook";
  private final WebHookContext context;
  private final WebHookMapper webHookMapper;
  private final RepositoryManager repositoryManager;

  @Inject
  public WebHookResource(WebHookContext context, WebHookMapper webHookMapper, RepositoryManager repositoryManager) {
    this.webHookMapper = webHookMapper;
    this.context = context;
    this.repositoryManager = repositoryManager;
  }

  @GET
  @Path("")
  @Produces(MediaType.APPLICATION_JSON)
  @Operation(
    summary = "Get global webhook configuration",
    description = "Returns the global webhook configuration.",
    tags = "Webhook Plugin",
    operationId = "webhook_get_global_config"
  )
  @ApiResponse(
    responseCode = "200",
    description = "success",
    content = @Content(
      mediaType = MediaType.APPLICATION_JSON,
      schema = @Schema(implementation = WebHookConfigurationDto.class)
    )
  )
  @ApiResponse(responseCode = "401", description = "not authenticated / invalid credentials")
  @ApiResponse(responseCode = "403", description = "not authorized, the current user does not have the \"configuration:read:webhook\" privilege")
  @ApiResponse(
    responseCode = "500",
    description = "internal server error",
    content = @Content(
      mediaType = VndMediaType.ERROR_TYPE,
      schema = @Schema(implementation = ErrorDto.class)
    )
  )
  public WebHookConfigurationDto getConfiguration() {
    WebHookContext.checkReadPermission();
    return webHookMapper.map(context.getGlobalConfiguration());
  }

  @POST
  @Path("")
  @Consumes(MediaType.APPLICATION_JSON)
  @Operation(
    summary = "Set global webhook configuration",
    description = "Sets the global webhook configuration.",
    tags = "Webhook Plugin",
    operationId = "webhook_set_global_config"
  )
  @ApiResponse(responseCode = "204", description = "no content")
  @ApiResponse(responseCode = "401", description = "not authenticated / invalid credentials")
  @ApiResponse(responseCode = "403", description = "not authorized, the current user does not have the \"configuration:read:webhook\" privilege")
  @ApiResponse(
    responseCode = "500",
    description = "internal server error",
    content = @Content(
      mediaType = VndMediaType.ERROR_TYPE,
      schema = @Schema(implementation = ErrorDto.class)
    )
  )
  public void setConfiguration(WebHookConfigurationDto configuration) {
    setConfigurations(configuration);
  }

  @PUT
  @Path("")
  @Consumes(MediaType.APPLICATION_JSON)
  @Operation(
    summary = "Update global webhook configuration",
    description = "Modifies the global webhook configuration.",
    tags = "Webhook Plugin",
    operationId = "webhook_update_global_config"
  )
  @ApiResponse(responseCode = "204", description = "update success")
  @ApiResponse(responseCode = "401", description = "not authenticated / invalid credentials")
  @ApiResponse(responseCode = "403", description = "not authorized, the current user does not have the \"configuration:write:webhook\" privilege")
  @ApiResponse(
    responseCode = "500",
    description = "internal server error",
    content = @Content(
      mediaType = VndMediaType.ERROR_TYPE,
      schema = @Schema(implementation = ErrorDto.class)
    )
  )
  public void updateConfiguration(WebHookConfigurationDto configuration) {
    setConfigurations(configuration);
  }

  @GET
  @Path("/{namespace}/{name}")
  @Produces(MediaType.APPLICATION_JSON)
  @Operation(
    summary = "Get webhook repository configuration",
    description = "Returns the repository specific webhook configuration.",
    tags = "Webhook Plugin",
    operationId = "webhook_get_repo_config"

  )
  @ApiResponse(
    responseCode = "200",
    description = "success",
    content = @Content(
      mediaType = MediaType.APPLICATION_JSON,
      schema = @Schema(implementation = WebHookConfigurationDto.class)
    )
  )
  @ApiResponse(responseCode = "401", description = "not authenticated / invalid credentials")
  @ApiResponse(responseCode = "403", description = "not authorized, the current user does not have the \"configuration:read:webhook\" privilege")
  @ApiResponse(
    responseCode = "404",
    description = "not found, no repository with the specified namespace and name available",
    content = @Content(
      mediaType = VndMediaType.ERROR_TYPE,
      schema = @Schema(implementation = ErrorDto.class)
    ))
  @ApiResponse(
    responseCode = "500",
    description = "internal server error",
    content = @Content(
      mediaType = VndMediaType.ERROR_TYPE,
      schema = @Schema(implementation = ErrorDto.class)
    )
  )
  public WebHookConfigurationDto getRepositoryConfiguration(@PathParam("namespace") String namespace, @PathParam("name") String name) {
    Repository repository = repositoryManager.get(new NamespaceAndName(namespace, name));
    WebHookContext.checkReadPermission(repository);
    return webHookMapper
      .map(context.getRepositoryConfigurations(namespace, name), repository);
  }

  @POST
  @Path("/{namespace}/{name}")
  @Consumes(MediaType.APPLICATION_JSON)
  @Operation(
    summary = "Set webhook repository configuration",
    description = "Sets the repository specific webhook configuration.",
    tags = "Webhook Plugin",
    operationId = "webhook_set_repo_config"
  )
  @ApiResponse(responseCode = "204", description = "update success")
  @ApiResponse(responseCode = "401", description = "not authenticated / invalid credentials")
  @ApiResponse(responseCode = "403", description = "not authorized, the current user does not have the \"configuration:read:webhook\" privilege")
  @ApiResponse(
    responseCode = "404",
    description = "not found, no repository with the specified namespace and name available",
    content = @Content(
      mediaType = VndMediaType.ERROR_TYPE,
      schema = @Schema(implementation = ErrorDto.class)
    ))
  @ApiResponse(
    responseCode = "500",
    description = "internal server error",
    content = @Content(
      mediaType = VndMediaType.ERROR_TYPE,
      schema = @Schema(implementation = ErrorDto.class)
    )
  )
  public void setRepositoryConfiguration(@PathParam("namespace") String namespace, @PathParam("name") String name, WebHookConfigurationDto configuration) {
    setRepositoryConfigurations(configuration, namespace, name);
  }

  @PUT
  @Path("/{namespace}/{name}")
  @Consumes(MediaType.APPLICATION_JSON)
  @Operation(
    summary = "Update webhook repository configuration",
    description = "Modifies the repository specific webhook configuration.",
    tags = "Webhook Plugin",
    operationId = "webhook_update_repo_config"
  )
  @ApiResponse(responseCode = "204", description = "update success")
  @ApiResponse(responseCode = "401", description = "not authenticated / invalid credentials")
  @ApiResponse(responseCode = "403", description = "not authorized, the current user does not have the \"configuration:write:webhook\" privilege")
  @ApiResponse(
    responseCode = "404",
    description = "not found, no repository with the specified namespace and name available",
    content = @Content(
      mediaType = VndMediaType.ERROR_TYPE,
      schema = @Schema(implementation = ErrorDto.class)
    ))
  @ApiResponse(
    responseCode = "500",
    description = "internal server error",
    content = @Content(
      mediaType = VndMediaType.ERROR_TYPE,
      schema = @Schema(implementation = ErrorDto.class)
    )
  )
  public void updateRepositoryConfiguration(@PathParam("namespace") String namespace, @PathParam("name") String name, WebHookConfigurationDto configuration) {
    setRepositoryConfigurations(configuration, namespace, name);
  }

  private void setConfigurations(WebHookConfigurationDto configuration) {
    WebHookContext.checkWritePermission();
    context.setGlobalConfiguration(webHookMapper.map(configuration));
  }

  private void setRepositoryConfigurations(WebHookConfigurationDto configuration, String namespace, String name) {
    Repository repository = repositoryManager.get(new NamespaceAndName(namespace, name));
    WebHookContext.checkWritePermission(repository);
    context.setRepositoryConfiguration(webHookMapper
      .map(configuration), namespace, name);
  }

}
