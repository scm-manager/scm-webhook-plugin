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
import { Button, Notification } from "@scm-manager/ui-components";
import { ExtensionPoint } from "@scm-manager/ui-extensions";
import { EditorStates } from "./WebHookConfigurationsForm";
import { useTranslation } from "react-i18next";

type Props = EditorStates & {
  readOnly: boolean;
  onChange: (changedWebHook, index, valid) => void;
  onDelete: (index) => void;
};

export const WebHookListConfigurationForm: FC<Props> = ({ editorStates, readOnly, onChange, onDelete }) => {
  const { t } = useTranslation("plugins");

  if (editorStates && editorStates.length > 0) {
    return (
      <>
        {editorStates.map((webHook, index) => {
          const deleteIcon = readOnly ? (
            ""
          ) : (
            <Button className="level-item" action={() => onDelete(index)}>
              <i className="fas fa-trash" />
            </Button>
          );
          return (
            <>
              <div className={"columns is-vcentered"} key={`config-${index}`}>
                <div className={"column"}>
                  <ExtensionPoint
                    name={`webhook.configuration.${webHook.name}`}
                    renderAll={false}
                    props={{
                      webHook: webHook,
                      readOnly: readOnly,
                      onChange: (changedWebHook, valid) => onChange(changedWebHook, index, valid)
                    }}
                  />
                </div>
                <div className={"column"}>{deleteIcon}</div>
              </div>
              <hr />
            </>
          );
        })}
      </>
    );
  } else {
    return <Notification type={"info"}>{t("scm-webhook-plugin.noHooksConfigured")}</Notification>;
  }
};
