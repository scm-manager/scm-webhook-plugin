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
          {t("config.form.webhooks.configuration.headers.title")}
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
