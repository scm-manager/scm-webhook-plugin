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
import { useTranslation } from "react-i18next";

import styled from "styled-components";
import { Checkbox, ConfirmAlert, Select, Help, Icon, InputField } from "@scm-manager/ui-components";
import { WebHookConfiguration } from "./WebHookConfiguration";

const DeleteIcon = styled(Icon)`
  margin: 0.55rem 0 0 0.75rem;
`;

type Props = {
  webHook: WebHookConfiguration;
  readOnly: boolean;
  onChange: (p: WebHookConfiguration) => void;
  onDelete: (p: WebHookConfiguration) => void;
};

const WebHookConfigurationForm: FC<Props> = ({ webHook, readOnly, onChange, onDelete }) => {
  const [t] = useTranslation("plugins");
  const [showConfirmAlert, setShowConfirmAlert] = useState(false);

  const handleChange = (value: string | boolean, name: string) => {
    onChange({
      ...webHook,
      [name]: value
    });
  };

  const options = ["GET", "POST", "AUTO", "PUT"];
  return (
    <>
      {showConfirmAlert ? (
        <ConfirmAlert
          title={t("scm-webhook-plugin.confirm-delete.title")}
          message={t("scm-webhook-plugin.confirm-delete.message")}
          buttons={[
            {
              className: "is-outlined",
              label: t("scm-webhook-plugin.confirm-delete.submit"),
              onClick: () => onDelete(webHook)
            },
            {
              label: t("scm-webhook-plugin.confirm-delete.cancel"),
              onClick: () => null,
              autofocus: true
            }
          ]}
          close={() => setShowConfirmAlert(false)}
        />
      ) : null}
      <article className="media">
        <div className="media-left mr-5 is-flex">
          <Select
            name="method"
            value={webHook.method}
            onChange={handleChange}
            options={options.map(v => ({ label: v, value: v }))}
            disabled={readOnly}
          />
          <Help message={t("scm-webhook-plugin.form.methodHelp")} />
        </div>
        <div className="media-content">
          <InputField
            name="urlPattern"
            value={webHook.urlPattern}
            onChange={handleChange}
            placeholder={t("scm-webhook-plugin.form.urlPattern")}
            disabled={readOnly}
          />
          <Checkbox
            name="executeOnEveryCommit"
            checked={webHook.executeOnEveryCommit}
            onChange={handleChange}
            label={t("scm-webhook-plugin.form.executeOnEveryCommit")}
            helpText={t("scm-webhook-plugin.form.executeOnEveryCommitHelp")}
            disabled={readOnly}
          />
          <Checkbox
            name="sendCommitData"
            checked={webHook.sendCommitData}
            onChange={handleChange}
            label={t("scm-webhook-plugin.form.sendCommitData")}
            helpText={t("scm-webhook-plugin.form.sendCommitDataHelp")}
            disabled={readOnly}
          />
        </div>
        <div>
          <Help message={t("scm-webhook-plugin.form.urlPatternHelp")} />
        </div>
        <div className="media-right">
          {!readOnly && (
            <DeleteIcon
              className="level-item"
              name="trash"
              title={t("scm-webhook-plugin.form.delete")}
              color="inherit"
              onClick={() => setShowConfirmAlert(true)}
            />
          )}
        </div>
      </article>
    </>
  );
};

export default WebHookConfigurationForm;
