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

import React, { FC } from "react";
import { Form } from "@scm-manager/ui-forms";
import { Icon } from "@scm-manager/ui-components";
import { useTranslation } from "react-i18next";

export type SimpleWebHookConfiguration = {
  urlPattern: string;
  executeOnEveryCommit: boolean;
  sendCommitData: boolean;
  method: string;
  headers: WebhookHeader[];
};

export type WebhookHeader = {
  key: string;
  value: string;
  concealed: boolean;
};

type Props = { webhook: SimpleWebHookConfiguration };

const SimpleWebHookConfigurationForm: FC<Props> = ({ webhook }) => {
  const [t] = useTranslation("plugins", {
    keyPrefix: "scm-webhook-plugin"
  });

  return (
    <>
      <Form.Row>
        <Form.Select
          name="method"
          options={[
            { value: "GET", label: "GET" },
            { value: "POST", label: "POST" },
            { value: "AUTO", label: "AUTO" },
            { value: "PUT", label: "PUT" }
          ]}
        />
      </Form.Row>
      <Form.Row>
        <Form.Input name="urlPattern" rules={{ required: true }} className="is-flex-grow-1" />
      </Form.Row>
      <Form.Row>
        <Form.Checkbox name="executeOnEveryCommit" />
      </Form.Row>
      <Form.Row>
        <Form.Checkbox name="sendCommitData" />
      </Form.Row>
      <details className="panel">
        <summary className="panel-heading is-size-6 is-clickable">
          {t("config.form.webhooks.configuration.headers.sectionTitle")}: {webhook?.headers?.length || 0}
        </summary>
        <div className="panel-block p-5">
          {
            <Form.ListContext name="headers">
              <Form.Table withDelete>
                <Form.Table.Column name="key" />
                <Form.Table.Column name="value">
                  {({ concealed, value }) => (concealed ? "●●●●●●●●●●●" : value)}
                </Form.Table.Column>
                <Form.Table.Column name="concealed">
                  {({ concealed }) => (concealed ? <Icon name="check" /> : null)}
                </Form.Table.Column>
              </Form.Table>
              <Form.AddListEntryForm defaultValues={{ key: "", value: "", concealed: false }}>
                <Form.Row>
                  <Form.Input
                    name="key"
                    rules={{
                      validate: newKey => !webhook.headers.some(({ key }) => newKey === key),
                      required: true
                    }}
                  />
                </Form.Row>
                <Form.Row>
                  <Form.Input name="value" />
                </Form.Row>
                <Form.Row>
                  <Form.Checkbox name="concealed" />
                </Form.Row>
              </Form.AddListEntryForm>
            </Form.ListContext>
          }
        </div>
      </details>
    </>
  );
};

export default SimpleWebHookConfigurationForm;
