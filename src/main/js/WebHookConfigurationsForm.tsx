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
import React from "react";
import { WithTranslation, withTranslation } from "react-i18next";
import { WebHookConfigurations } from "./WebHookConfiguration";
import { Button, Level, confirmAlert } from "@scm-manager/ui-components";
import { ExtensionPoint } from "@scm-manager/ui-extensions";
import AddWebHookButton from "./AddWebHookButton";

type Props = WithTranslation & {
  initialConfiguration: WebHookConfigurations;
  readOnly: boolean;
  onConfigurationChange: (p1: WebHookConfigurations, p2: boolean) => void;
};

type State = WebHookConfigurations & {};

class WebHookConfigurationsForm extends React.Component<Props, State> {
  constructor(props: Props) {
    super(props);
    this.state = {
      ...props.initialConfiguration
    };
  }

  isValid() {
    const { webhooks } = this.state;
    let valid = true;
    webhooks.map(webHook => {
      valid = valid && webHook.configuration.urlPattern.trim() !== "" && webHook.configuration.method.trim() !== "";
    });
    return valid;
  }

  updateWebHooks(webhooks) {
    this.setState(
      {
        webhooks
      },
      () => this.props.onConfigurationChange(this.state, this.isValid())
    );
  }

  confirmDelete = (index: number) => {
    const { t } = this.props;
    confirmAlert({
      title: t("scm-webhook-plugin.confirm-delete.title"),
      message: t("scm-webhook-plugin.confirm-delete.message"),
      buttons: [
        {
          label: t("scm-webhook-plugin.confirm-delete.submit"),
          onClick: () => this.onDelete(index)
        },
        {
          className: "is-info",
          label: t("scm-webhook-plugin.confirm-delete.cancel"),
          onClick: () => null
        }
      ]
    });
  };

  onDelete = index => {
    const { webhooks } = this.state;
    webhooks.splice(index, 1);
    this.updateWebHooks(webhooks);
  };

  onChange = (changedWebHook, index) => {
    const { webhooks } = this.state;
    webhooks[index].configuration = changedWebHook;
    this.updateWebHooks(webhooks);
  };

  render() {
    const { webhooks } = this.state;
    const { readOnly } = this.props;

    return (
      <>
        {webhooks.map((webHook, index) => {
          const deleteIcon = readOnly ? (
            ""
          ) : (
            <Button className="level-item" action={() => this.confirmDelete(index)}>
              <i className="fas fa-trash" />
            </Button>
          );
          return (
            <div className={"columns is-vcentered"}>
              <div className={"column"}>
                <ExtensionPoint
                  name={`webhook.configuration.${webHook.name}`}
                  renderAll={true}
                  props={{
                    webHook: webHook,
                    readOnly: readOnly,
                    onChange: changedWebHook => this.onChange(changedWebHook, index)
                  }}
                />
              </div>
              <div className={"colum"}>{deleteIcon}</div>
            </div>
          );
        })}
        <Level
          right={
            <AddWebHookButton
              readOnly={readOnly}
              onAdd={({ name, defaultConfiguration }) => {
                webhooks.push({
                  name: name,
                  configuration: defaultConfiguration
                });
                this.updateWebHooks(webhooks);
              }}
            />
          }
        />
      </>
    );
  }
}

export default withTranslation("plugins")(WebHookConfigurationsForm);
