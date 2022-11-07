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
import { Level, AddButton } from "@scm-manager/ui-components";
import { ExtensionPoint } from "@scm-manager/ui-extensions";
import styled from "styled-components";

type Props = WithTranslation & {
  initialConfiguration: WebHookConfigurations;
  readOnly: boolean;
  onConfigurationChange: (p1: WebHookConfigurations, p2: boolean) => void;
};

type State = WebHookConfigurations & {};

const DeleteIcon = styled.a`
  margin: 0.55rem 0 0 0.75rem;
`;

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

  onDelete = deletedWebHook => {
    const { webhooks } = this.state;
    const index = webhooks.indexOf(deletedWebHook);
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
    const { t, readOnly } = this.props;
    const defaultWebHook = {
      urlPattern: "",
      executeOnEveryCommit: false,
      sendCommitData: false,
      method: "AUTO"
    };

    return (
      <>
        {webhooks.map((webHook, index) => {
          const deleteIcon = readOnly ? (
            ""
          ) : (
            <DeleteIcon className="level-item" onClick={this.confirmDelete}>
              <span className="icon is-small">
                <i className="fas fa-trash" />
              </span>
            </DeleteIcon>
          );
          return (
            <>
              <ExtensionPoint
                name={`webhook.configuration.${webHook.name}`}
                renderAll={true}
                props={{
                  webHook: webHook,
                  readOnly: readOnly,
                  onChange: changedWebHook => this.onChange(changedWebHook, index),
                  // configurationChanged: validatorConfigChanged}
                }}
              />
              <div>{deleteIcon}</div>
            </>
          );
        })}
        <Level right={<AddButton
            disabled={readOnly}
            label={t("scm-webhook-plugin.add")}
            action={() => {
              webhooks.push(defaultWebHook);
              this.updateWebHooks(webhooks);
            }}
          />}
        />
      </>
    );
  }
}

export default withTranslation("plugins")(WebHookConfigurationsForm);
