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
import React, { FC, useMemo } from "react";
import { Notification, Subtitle, Title } from "@scm-manager/ui-components";
import { useTranslation } from "react-i18next";
import { Repository } from "@scm-manager/ui-types";
import { ConfigurationForm, Form } from "@scm-manager/ui-forms";
import { useBinder } from "@scm-manager/ui-extensions";
import { WebhookConfiguration } from "./extensionPoints";

type Props = {
  repository?: Repository;
  title?: string;
  subtitle?: string;
  link: string;
};

const WebHookConfiguration: FC<Props> = ({ repository, title, subtitle, link }) => {
  const [t] = useTranslation("plugins");
  const binder = useBinder();
  const allWebHooks = binder.getExtensions<WebhookConfiguration>("webhook.configuration");
  const webhookMap = useMemo<Record<string, WebhookConfiguration["type"]>>(
    () =>
      allWebHooks.reduce((prev, cur) => {
        prev[cur.name] = cur;
        return prev;
      }, {}),
    [allWebHooks]
  );
  const options = useMemo(
    () =>
      (repository?._embedded?.supportedWebHookTypes.types ?? allWebHooks.map(({ name }) => name)).map(name => ({
        value: name,
        label: t(`webhooks.${name}.name`)
      })),
    [allWebHooks, repository, t]
  );

  return (
    <>
      {title ? <Title>{title}</Title> : null}
      {subtitle ? <Subtitle>{subtitle}</Subtitle> : null}
      <h2>{t("scm-webhook-plugin.config.helpText")}</h2>
      <br />
      <ConfigurationForm link={link} translationPath={["plugins", "scm-webhook-plugin.config.form"]}>
        {({ watch }) => (
          <Form.ListContext name="webhooks">
            {watch("webhooks").length ? (
              <Form.List withDelete>
                {({ value: webhook }) => (
                  <Form.PathContext path="configuration">
                    {webhook.name in webhookMap ? (
                      React.createElement(webhookMap[webhook.name].FormComponent, { webhook: webhook.configuration })
                    ) : (
                      <Notification type="warning">
                        {t("scm-webhook-plugin.config.unknownConfigurationType")}
                      </Notification>
                    )}
                  </Form.PathContext>
                )}
              </Form.List>
            ) : (
              <Notification type="info">{t("scm-webhook-plugin.config.noHooksConfigured")}</Notification>
            )}
            <Form.AddListEntryForm
              disableSubmitWhenDirty={false}
              defaultValues={{ name: "SimpleWebHook" }}
              submit={(data, append) => {
                const { defaultConfiguration } = webhookMap[data.name as string];
                append({ name: data.name, configuration: defaultConfiguration });
              }}
            >
              <Form.Select name="name" options={options} />
            </Form.AddListEntryForm>
          </Form.ListContext>
        )}
      </ConfigurationForm>
    </>
  );
};

export default WebHookConfiguration;
