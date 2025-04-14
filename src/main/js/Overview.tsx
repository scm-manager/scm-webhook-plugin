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

import React, { FC, useMemo, useState } from "react";
import { Notification, Subtitle, useDocumentTitle } from "@scm-manager/ui-core";
import { useTranslation } from "react-i18next";
import { WebhookConfiguration } from "./extensionPoints";
import PrimaryInformation from "./PrimaryInformation";
import { WebHookConfiguration } from "./types";
import { Select } from "@scm-manager/ui-forms";
import { Icon, LinkButton } from "@scm-manager/ui-buttons";
import { Menu } from "@scm-manager/ui-overlays";
import { Repository } from "@scm-manager/ui-types";

type Props = {
  repository?: Repository;
  webhooks: WebHookConfiguration[];
  webhookMap: Record<string, WebhookConfiguration["type"]>;
  typeOptions: any;
  isReadOnly?: boolean;
  onDelete: (webhook: WebHookConfiguration) => Promise<unknown>;
};

const Overview: FC<Props> = ({ repository, webhooks, webhookMap, typeOptions, isReadOnly, onDelete }) => {
  const [t] = useTranslation("plugins");
  const [type, setType] = useState("ALL");

  // We can't use useDocumentTitleForRepository here since this would mean conditional hooks.
  // TODO Needs to be fixed with a future SCM-Core version.
  useDocumentTitle(t("scm-webhook-plugin.config.title"));

  const filteredWebhooks = useMemo(
    () => (type === "ALL" ? webhooks : webhooks.filter((webhook) => webhook.name === type)),
    [type, webhooks],
  );

  const options = useMemo(
    () => [{ label: t("scm-webhook-plugin.config.filter.all"), value: "ALL" }, ...typeOptions],
    [typeOptions],
  );

  return (
    <>
      {repository ? <Subtitle>{t("scm-webhook-plugin.config.title")}</Subtitle> : null}
      <div className="columns is-justify-content-space-between is-align-items-center">
        <div className="column is-flex is-align-items-baseline">
          <strong className="mr-3">{t("scm-webhook-plugin.config.filter.label")}</strong>
          <Select value={type} onChange={(event) => setType(event.target.value)} options={options} />
        </div>
        {!isReadOnly ? (
          <div className="column is-flex is-justify-content-flex-end">
            <LinkButton variant="primary" to="webhook/add">
              {t("scm-webhook-plugin.config.button.add")}
            </LinkButton>
          </div>
        ) : null}
      </div>
      <div className="box">
        {filteredWebhooks?.length > 0 ? (
          filteredWebhooks.map((webhook, idx) => (
            <>
              {idx > 0 ? <hr className="my-3" /> : null}
              {webhook.name in webhookMap ? (
                <div className="is-flex is-flex-direction-column">
                  <div className="mb-1 is-flex is-justify-content-space-between">
                    <div className="is-flex is-align-items-center">
                      <PrimaryInformation>{webhook.name}</PrimaryInformation>
                      {webhookMap[webhook.name].OverviewCardTop
                        ? React.createElement(webhookMap[webhook.name].OverviewCardTop, {
                            webhook: webhook.configuration,
                          })
                        : null}
                    </div>
                    <Menu>
                      <Menu.Link to={`webhook/${webhook.id}`}>
                        <Icon>edit</Icon>
                        {t("scm-webhook-plugin.config.button.edit")}
                      </Menu.Link>
                      <Menu.Button onSelect={() => onDelete(webhook)}>
                        <Icon>trash</Icon>
                        {t("scm-webhook-plugin.config.button.delete")}
                      </Menu.Button>
                    </Menu>
                  </div>
                  <div>
                    {webhookMap[webhook.name].OverviewCardBottom
                      ? React.createElement(webhookMap[webhook.name].OverviewCardBottom, {
                          webhook: webhook.configuration,
                        })
                      : null}
                  </div>
                </div>
              ) : (
                <Notification type="warning">{t("scm-webhook-plugin.config.unknownConfigurationType")}</Notification>
              )}
            </>
          ))
        ) : (
          <Notification type="info">
            {t(
              webhooks.length > 0
                ? "scm-webhook-plugin.config.noHooksForFilter"
                : "scm-webhook-plugin.config.noHooksConfigured",
            )}
          </Notification>
        )}
      </div>
    </>
  );
};

export default Overview;
