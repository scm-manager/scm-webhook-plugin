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
import com.webcohesion.enunciate.metadata.rs.ResponseCode;
import com.webcohesion.enunciate.metadata.rs.StatusCodes;
import sonia.scm.repository.NamespaceAndName;
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
@Path(WebHookResource.PATH)
public class WebHookResource {

  public static final String PATH = "v2/plugins/webhook";
  private final WebHookContext context;
  private final WebHookMapper webHookMapper;

  @Inject
  public WebHookResource(WebHookContext context, WebHookMapper webHookMapper) {
    this.webHookMapper = webHookMapper;
    this.context = context;
  }

  @GET
  @Path("")
  @Produces(MediaType.APPLICATION_JSON)
  @StatusCodes({
    @ResponseCode(code = 200, condition = "success"),
    @ResponseCode(code = 401, condition = "not authenticated / invalid credentials"),
    @ResponseCode(code = 403, condition = "not authorized, the current user does not have the \"configuration:read:webhook\" privilege"),
    @ResponseCode(code = 500, condition = "internal server error")
  })
  public WebHookConfigurationDto getConfiguration(@Context UriInfo uriInfo) {
    WebHookContext.checkReadPermission();
    return webHookMapper.using(uriInfo).map(context.getGlobalConfiguration());
  }

  @POST
  @Path("")
  @Consumes(MediaType.APPLICATION_JSON)
  @StatusCodes({
    @ResponseCode(code = 204, condition = "no content"),
    @ResponseCode(code = 401, condition = "not authenticated / invalid credentials"),
    @ResponseCode(code = 403, condition = "not authorized, the current user does not have the \"configuration:read:webhook\" privilege"),
    @ResponseCode(code = 500, condition = "internal server error")
  })
  public void setConfiguration(@Context UriInfo uriInfo, WebHookConfigurationDto configuration) {
    setConfigurations(uriInfo, configuration);
  }

  @PUT
  @Path("")
  @Consumes(MediaType.APPLICATION_JSON)
  @StatusCodes({
    @ResponseCode(code = 204, condition = "update success"),
    @ResponseCode(code = 401, condition = "not authenticated / invalid credentials"),
    @ResponseCode(code = 403, condition = "not authorized, the current user does not have the \"configuration:write:webhook\" privilege"),
    @ResponseCode(code = 500, condition = "internal server error")
  })
  public void updateConfiguration(@Context UriInfo uriInfo, WebHookConfigurationDto configuration) {
    setConfigurations(uriInfo, configuration);
  }

  @GET
  @Path("/{namespace}/{name}")
  @Produces(MediaType.APPLICATION_JSON)
  @StatusCodes({
    @ResponseCode(code = 200, condition = "success"),
    @ResponseCode(code = 401, condition = "not authenticated / invalid credentials"),
    @ResponseCode(code = 403, condition = "not authorized, the current user does not have the \"configuration:read:webhook\" privilege"),
    @ResponseCode(code = 500, condition = "internal server error")
  })
  public WebHookConfigurationDto getRepositoryConfiguration(@Context UriInfo uriInfo, @PathParam("namespace") String namespace, @PathParam("name") String name) {
    WebHookContext.checkReadPermission();
    return webHookMapper
      .using(uriInfo)
      .forRepository(new NamespaceAndName(namespace, name))
      .map(context.getRepositoryConfigurations(namespace, name));
  }

  @POST
  @Path("/{namespace}/{name}")
  @Consumes(MediaType.APPLICATION_JSON)
  @StatusCodes({
    @ResponseCode(code = 204, condition = "no content"),
    @ResponseCode(code = 401, condition = "not authenticated / invalid credentials"),
    @ResponseCode(code = 403, condition = "not authorized, the current user does not have the \"configuration:read:webhook\" privilege"),
    @ResponseCode(code = 500, condition = "internal server error")
  })
  public void setRepositoryConfiguration(@Context UriInfo uriInfo, @PathParam("namespace") String namespace, @PathParam("name") String name, WebHookConfigurationDto configuration) {
    setRepositoryConfigurations(uriInfo, configuration, namespace, name);
  }

  @PUT
  @Path("/{namespace}/{name}")
  @Consumes(MediaType.APPLICATION_JSON)
  @StatusCodes({
    @ResponseCode(code = 204, condition = "update success"),
    @ResponseCode(code = 401, condition = "not authenticated / invalid credentials"),
    @ResponseCode(code = 403, condition = "not authorized, the current user does not have the \"configuration:write:webhook\" privilege"),
    @ResponseCode(code = 500, condition = "internal server error")
  })
  public void updateRepositoryConfiguration(@Context UriInfo uriInfo, @PathParam("namespace") String namespace, @PathParam("name") String name, WebHookConfigurationDto configuration) {
    setRepositoryConfigurations(uriInfo, configuration, namespace, name);
  }

  private void setConfigurations(UriInfo uriInfo, WebHookConfigurationDto configuration) {
    WebHookContext.checkWritePermission();
    context.setGlobalConfiguration(webHookMapper.using(uriInfo).map(configuration));
  }

  private void setRepositoryConfigurations(UriInfo uriInfo, WebHookConfigurationDto configuration, String namespace, String name) {
    WebHookContext.checkWritePermission();
    context.setRepositoryConfiguration(webHookMapper
      .using(uriInfo)
      .forRepository(new NamespaceAndName(namespace, name))
      .map(configuration), namespace, name);
  }

}
