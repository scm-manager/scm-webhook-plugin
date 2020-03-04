/**
 * Copyright (c) 2010, Sebastian Sdorra All rights reserved.
 * <p>
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * <p>
 * 1. Redistributions of source code must retain the above copyright notice,
 * this list of conditions and the following disclaimer. 2. Redistributions in
 * binary form must reproduce the above copyright notice, this list of
 * conditions and the following disclaimer in the documentation and/or other
 * materials provided with the distribution. 3. Neither the name of SCM-Manager;
 * nor the names of its contributors may be used to endorse or promote products
 * derived from this software without specific prior written permission.
 * <p>
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE REGENTS OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 * <p>
 * http://bitbucket.org/sdorra/scm-manager
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

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriInfo;

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
  public WebHookConfigurationDto getConfiguration(@Context UriInfo uriInfo) {
    WebHookContext.checkReadPermission();
    return webHookMapper.using(uriInfo).map(context.getGlobalConfiguration());
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
  public void setConfiguration(@Context UriInfo uriInfo, WebHookConfigurationDto configuration) {
    setConfigurations(uriInfo, configuration);
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
  public void updateConfiguration(@Context UriInfo uriInfo, WebHookConfigurationDto configuration) {
    setConfigurations(uriInfo, configuration);
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
  public WebHookConfigurationDto getRepositoryConfiguration(@Context UriInfo uriInfo, @PathParam("namespace") String namespace, @PathParam("name") String name) {
    Repository repository = repositoryManager.get(new NamespaceAndName(namespace, name));
    WebHookContext.checkReadPermission(repository);
    return webHookMapper
      .using(uriInfo)
      .forRepository(repository)
      .map(context.getRepositoryConfigurations(namespace, name));
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
  public void setRepositoryConfiguration(@Context UriInfo uriInfo, @PathParam("namespace") String namespace, @PathParam("name") String name, WebHookConfigurationDto configuration) {
    setRepositoryConfigurations(uriInfo, configuration, namespace, name);
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
  public void updateRepositoryConfiguration(@Context UriInfo uriInfo, @PathParam("namespace") String namespace, @PathParam("name") String name, WebHookConfigurationDto configuration) {
    setRepositoryConfigurations(uriInfo, configuration, namespace, name);
  }

  private void setConfigurations(UriInfo uriInfo, WebHookConfigurationDto configuration) {
    WebHookContext.checkWritePermission();
    context.setGlobalConfiguration(webHookMapper.using(uriInfo).map(configuration));
  }

  private void setRepositoryConfigurations(UriInfo uriInfo, WebHookConfigurationDto configuration, String namespace, String name) {
    Repository repository = repositoryManager.get(new NamespaceAndName(namespace, name));
    WebHookContext.checkWritePermission(repository);
    context.setRepositoryConfiguration(webHookMapper
      .using(uriInfo)
      .forRepository(repository)
      .map(configuration), namespace, name);
  }

}
