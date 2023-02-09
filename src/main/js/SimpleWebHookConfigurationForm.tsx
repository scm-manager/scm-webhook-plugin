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
import React, { FC, useState } from "react";
import { Checkbox, Help, HelpIcon, Icon, InputField, Select, Tooltip } from "@scm-manager/ui-components";
import { SimpleWebHookConfiguration, WebHookConfiguration, WebhookHeader } from "./WebHookConfiguration";
import { useTranslation } from "react-i18next";
import { Button } from "@scm-manager/ui-buttons";

type Props = {
  webHook: WebHookConfiguration;
  readOnly: boolean;
  onChange: (p: SimpleWebHookConfiguration, isValid: boolean) => void;
};

const SimpleWebHookConfigurationForm: FC<Props> = ({ webHook, readOnly, onChange }) => {
  const [t] = useTranslation("plugins");
  const [webhookConfig, setWebhookConfig] = useState({
    ...(webHook.configuration as SimpleWebHookConfiguration),
    headers: webHook.configuration.headers || []
  });
  const [newHeader, setNewHeader] = useState<WebhookHeader>({ key: "", value: "", concealed: false });
  const [showTable, setShowTable] = useState(false);

  const isValid = () => {
    return webhookConfig.urlPattern.trim() !== "" && webhookConfig.method.trim() !== "";
  };

  const handleChange = (value: any, name: string) => {
    setWebhookConfig({ ...webhookConfig, [name]: value });
    onChange({ ...webhookConfig, [name]: value }, isValid());
  };

  const renderHeaderTable = () => {
    return (
      <div className="table-container">
        <table className="table">
          <thead>
            <th>
              {t("scm-webhook-plugin.form.headerKey")}
              <Tooltip location="bottom" multiline={true} message={t("scm-webhook-plugin.form.headerKeyHelpText")}>
                <HelpIcon />
              </Tooltip>
            </th>
            <th>
              {t("scm-webhook-plugin.form.headerValue")}
              <Tooltip location="bottom" multiline={true} message={t("scm-webhook-plugin.form.headerValueHelpText")}>
                <HelpIcon />
              </Tooltip>
            </th>
            <th>
              {t("scm-webhook-plugin.form.headerConcealed")}
              <Tooltip
                location="bottom"
                multiline={true}
                message={t("scm-webhook-plugin.form.headerConcealedHelpText")}
              >
                <HelpIcon />
              </Tooltip>
            </th>
            <th />
          </thead>
          <tbody>
            {webhookConfig.headers?.map((header: WebhookHeader, index) => (
              <tr key={header?.key || `header_${index}`}>
                <th>{header.key}</th>
                <td>{header.concealed ? "●●●●●●●●●●●" : header.value}</td>
                <td>{header.concealed ? <Icon name="check" /> : null}</td>
                <td>
                  <Button
                    onClick={() => {
                      setWebhookConfig({
                        ...webhookConfig,
                        headers: webhookConfig.headers.filter(h => h.key != header.key)
                      });
                      onChange(webhookConfig, isValid());
                    }}
                    title={t("scm-webhook-plugin.form.removeHeader")}
                  >
                    <Icon name="times" />
                  </Button>
                </td>
              </tr>
            ))}
            <tr>
              <td>
                <InputField
                  value={newHeader.key}
                  onChange={key => setNewHeader({ ...newHeader, key: key.replace(" ", "") })}
                  validationError={webhookConfig.headers.some(h => h.key === newHeader.key)}
                  errorMessage={t("scm-webhook-plugin.form.headerKeyValidation")}
                />
              </td>
              <td>
                <InputField value={newHeader.value} onChange={value => setNewHeader({ ...newHeader, value })} />
              </td>
              <td>
                <Checkbox
                  checked={newHeader.concealed}
                  onChange={concealed => setNewHeader({ ...newHeader, concealed })}
                />
              </td>
              <td>
                <Button
                  onClick={() => {
                    setWebhookConfig({ ...webhookConfig, headers: [...webhookConfig.headers, newHeader] });
                    setNewHeader({ key: "", value: "", concealed: false });
                    onChange({ ...webhookConfig, headers: [...webhookConfig.headers, newHeader] }, isValid());
                  }}
                  disabled={
                    !newHeader.key || !newHeader.value || webhookConfig?.headers?.find(h => h?.key === newHeader?.key)
                  }
                  title={t("scm-webhook-plugin.form.addNewHeader")}
                >
                  <Icon name="plus" />
                </Button>
              </td>
            </tr>
          </tbody>
        </table>
      </div>
    );
  };

  return (
    <>
      <article className="media">
        <div className="media-left mr-2">
          {
            <div className="field control is-flex">
              <Select
                options={[
                  { value: "GET", label: "GET" },
                  { value: "POST", label: "POST" },
                  { value: "AUTO", label: "AUTO" },
                  { value: "PUT", label: "PUT" }
                ]}
                onChange={value => setWebhookConfig({ ...webhookConfig, method: value })}
                value={webhookConfig.method}
                disabled={readOnly}
              />
              <Help message={t("scm-webhook-plugin.form.methodHelp")} />
            </div>
          }
        </div>
        <div className="media-content">
          <InputField
            name="urlPattern"
            placeholder={t("scm-webhook-plugin.form.urlPattern")}
            value={webhookConfig.urlPattern}
            onChange={handleChange}
            disabled={readOnly}
          />
          <Checkbox
            name="executeOnEveryCommit"
            label={t("scm-webhook-plugin.form.executeOnEveryCommit")}
            checked={webhookConfig.executeOnEveryCommit}
            onChange={handleChange}
            disabled={readOnly}
            helpText={t("scm-webhook-plugin.form.executeOnEveryCommitHelp")}
          />
          <Checkbox
            name="sendCommitData"
            label={t("scm-webhook-plugin.form.sendCommitData")}
            checked={webhookConfig.sendCommitData}
            onChange={handleChange}
            disabled={readOnly}
            helpText={t("scm-webhook-plugin.form.sendCommitDataHelp")}
          />
        </div>
        <div>
          <Help message={t("scm-webhook-plugin.form.urlPatternHelp")} />
        </div>
      </article>
      <div className="panel">
        <div className="panel-heading is-size-6 is-clickable" onClick={() => setShowTable(!showTable)}>
          {showTable ? <Icon name="chevron-down" className="mr-1" /> : <Icon name="chevron-right" className="mr-1" />}
          {t("scm-webhook-plugin.form.additionalHeaders")}
        </div>
        {showTable ? <div className="panel-block">{showTable ? renderHeaderTable() : null}</div> : null}
      </div>
    </>
  );
};

export default SimpleWebHookConfigurationForm;
