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
import React, { FC, useMemo, useState } from "react";
import { Notification, Subtitle } from "@scm-manager/ui-components";
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
  const filteredWebhooks = useMemo(
    () => (type === "ALL" ? webhooks : webhooks.filter(webhook => webhook.name === type)),
    [type, webhooks]
  );

  const options = useMemo(
    () => [{ label: t("scm-webhook-plugin.config.filter.all"), value: "ALL" }, ...typeOptions],
    [typeOptions]
  );

  return (
    <>
      {repository ? <Subtitle>{t("scm-webhook-plugin.config.title")}</Subtitle> : null}
      <div className="columns is-justify-content-space-between is-align-items-center">
        <div className="column is-flex is-align-items-baseline">
          <strong className="mr-3">{t("scm-webhook-plugin.config.filter.label")}</strong>
          <Select
            value={type}
            onChange={event => setType(event.target.value)}
            options={options}
          />
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
                            webhook: webhook.configuration
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
                          webhook: webhook.configuration
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
                : "scm-webhook-plugin.config.noHooksConfigured"
            )}
          </Notification>
        )}
      </div>
    </>
  );
};

export default Overview;
