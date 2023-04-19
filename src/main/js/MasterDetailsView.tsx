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
import { Loading, Subtitle, Title } from "@scm-manager/ui-components";
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
  const { initialConfiguration, isReadOnly, update, isLoading, isUpdating } = useConfigLink<WebHookConfigurations>(
    link
  );
  const allWebHooks = binder.getExtensions<WebhookConfiguration>("webhook.configuration");
  const create = useCallback(
    (name: string, configuration: unknown) => {
      const newWebhook = {
        name,
        configuration
      } as WebHookConfiguration;
      initialConfiguration.webhooks.push(newWebhook);
      return update(initialConfiguration);
    },
    [initialConfiguration, update]
  );

  const updateWebhook = useCallback(
    (webhook: WebHookConfiguration) => {
      initialConfiguration.webhooks[initialConfiguration.webhooks.findIndex(wh => wh.id === webhook.id)] = webhook;
      return update(initialConfiguration);
    },
    [initialConfiguration, update]
  );

  const deleteWebhook = useCallback(
    (webhook: WebHookConfiguration) => {
      initialConfiguration.webhooks.splice(
        initialConfiguration.webhooks.findIndex(wh => wh.id === webhook.id),
        1
      );
      return update(initialConfiguration);
    },
    [initialConfiguration, update]
  );

  const webhookMap = useMemo<Record<string, WebhookConfiguration["type"]>>(
    () =>
      allWebHooks.reduce((prev, cur) => {
        prev[cur.name] = cur;
        return prev;
      }, {}),
    [allWebHooks]
  );

  const typeOptions = useMemo(
    () =>
      (repository?._embedded?.supportedWebHookTypes.types ?? allWebHooks.map(({ name }) => name)).map(name => ({
        value: name,
        label: t(`webhooks.${name}.name`)
      })),
    [allWebHooks, repository, t]
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
