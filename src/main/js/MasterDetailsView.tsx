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

import React, { FC, useCallback, useMemo } from "react";
import { Route, Switch } from "react-router-dom";
import Overview from "./Overview";
import CreatePage from "./CreatePage";
import { useBinder } from "@scm-manager/ui-extensions";
import { WebhookConfiguration } from "./extensionPoints";
import { Repository } from "@scm-manager/ui-types";
import { useTranslation } from "react-i18next";
import { useConfigLink } from "@scm-manager/ui-api";
import { WebHookConfiguration, WebHookConfigurations } from "./types";
import { Loading, Title } from "@scm-manager/ui-core";
import EditPage from "./EditPage";

type Props = {
  baseRoute: string;
  link: string;
  title?: string;
  repository?: Repository;
};

const MasterDetailsView: FC<Props> = ({ repository, link, title, baseRoute }) => {
  const [t] = useTranslation("plugins");
  const binder = useBinder();
  const { initialConfiguration, isReadOnly, update, isLoading, isUpdating } =
    useConfigLink<WebHookConfigurations>(link);
  const allWebHooks = binder.getExtensions<WebhookConfiguration>("webhook.configuration");

  const create = useCallback(
    (name: string, configuration: unknown) => {
      const newWebhook = {
        name,
        configuration,
      } as WebHookConfiguration;
      initialConfiguration.webhooks.push(newWebhook);
      return update(initialConfiguration);
    },
    [initialConfiguration, update],
  );

  const updateWebhook = useCallback(
    (webhook: WebHookConfiguration) => {
      initialConfiguration.webhooks[initialConfiguration.webhooks.findIndex((wh) => wh.id === webhook.id)] = webhook;
      return update(initialConfiguration);
    },
    [initialConfiguration, update],
  );

  const deleteWebhook = useCallback(
    (webhook: WebHookConfiguration) => {
      initialConfiguration.webhooks.splice(
        initialConfiguration.webhooks.findIndex((wh) => wh.id === webhook.id),
        1,
      );
      return update(initialConfiguration);
    },
    [initialConfiguration, update],
  );

  const webhookMap = useMemo<Record<string, WebhookConfiguration["type"]>>(
    () =>
      allWebHooks.reduce((prev, cur) => {
        prev[cur.name] = cur;
        return prev;
      }, {}),
    [allWebHooks],
  );

  const typeOptions = useMemo(
    () =>
      (repository?._embedded?.supportedWebHookTypes.types ?? allWebHooks.map(({ name }) => name)).map((name) => ({
        value: name,
        label: t(`webhooks.${name}.name`),
      })),
    [allWebHooks, repository, t],
  );

  if (isLoading || isUpdating) {
    return <Loading />;
  }

  return (
    <>
      {title ? <Title>{title}</Title> : null}
      <Switch>
        <Route path={`${baseRoute}/add`}>
          <CreatePage webhookMap={webhookMap} typeOptions={typeOptions} onCreate={create} baseRoute={baseRoute} />
        </Route>
        <Route path={`${baseRoute}/:id`}>
          <EditPage
            webhooks={initialConfiguration.webhooks}
            webhookMap={webhookMap}
            onUpdate={updateWebhook}
            baseRoute={baseRoute}
            onDelete={deleteWebhook}
          />
        </Route>
        <Route path={baseRoute} exact>
          <Overview
            repository={repository}
            webhooks={initialConfiguration.webhooks}
            webhookMap={webhookMap}
            typeOptions={typeOptions}
            isReadOnly={isReadOnly}
            onDelete={deleteWebhook}
          />
        </Route>
      </Switch>
    </>
  );
};

export default MasterDetailsView;
